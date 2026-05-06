package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Slf4j
@Component
public class CreateQuestCommand implements ICommandImpl {

    private static final int DISCORD_CHANNEL_NAME_LIMIT = 100;
    private static final long VOICE_GUIDE_ROLE_ID = 1426658009974837409L;
    private static final String NAME_OPTION = "name";
    private static final String NPCS_OPTION = "npcs";

    private final Executor questSetupExecutor;
    private final ConcurrentMap<String, Object> questLocks = new ConcurrentHashMap<>();

    public CreateQuestCommand(@Qualifier("questSetupExecutor") Executor questSetupExecutor) {
        this.questSetupExecutor = questSetupExecutor;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("createquest", "Creates a recording collection channel and NPC threads")
                .addOptions(
                        new OptionData(OptionType.STRING, NAME_OPTION, "The quest name", true),
                        new OptionData(OptionType.STRING, NPCS_OPTION, "NPC names separated by semicolons", true));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();

        String questName = Objects.requireNonNull(event.getOption(NAME_OPTION)).getAsString().trim();
        List<String> npcNames = parseNpcNames(Objects.requireNonNull(event.getOption(NPCS_OPTION)).getAsString());

        try {
            questSetupExecutor.execute(() -> setupQuest(event, questName, npcNames));
        } catch (RejectedExecutionException exception) {
            log.error("Failed scheduling quest setup for {}", questName, exception);
            event.getHook().editOriginal("The quest setup queue is full. Please try again shortly.").queue();
        }
    }

    private void setupQuest(SlashCommandInteractionEvent event, String questName, List<String> npcNames) {
        String validationError = validateInput(questName, npcNames);
        if (validationError != null) {
            event.getHook().editOriginal(validationError).queue();
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            event.getHook().editOriginal("This command can only be used in a server.").queue();
            return;
        }

        Category recordingCollectionCategory = guild.getCategoryById(Config.RECORDING_COLLECTION_CATEGORY_ID);
        if (recordingCollectionCategory == null) {
            event.getHook().editOriginal("Could not find the recording collection category.").queue();
            return;
        }

        List<String> createdThreads = new ArrayList<>();
        List<String> skippedThreads = new ArrayList<>();

        try {
            String lockKey = getQuestLockKey(guild, questName);
            Object lock = questLocks.computeIfAbsent(lockKey, key -> new Object());
            TextChannel questChannel;

            synchronized (lock) {
                questChannel = getOrCreateQuestChannel(guild, recordingCollectionCategory, questName);
                Set<String> existingThreadNames = getExistingThreadNames(questChannel);

                for (String npcName : npcNames) {
                    String normalizedNpcName = npcName.toLowerCase(Locale.ROOT);
                    if (existingThreadNames.contains(normalizedNpcName)) {
                        skippedThreads.add(npcName);
                        continue;
                    }

                    ThreadChannel threadChannel = questChannel.createThreadChannel(npcName, true)
                            .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK)
                            .complete();
                    threadChannel.sendMessage(getSubmissionMessage(guild, questChannel, questName, npcName)).complete();
                    createdThreads.add(npcName);
                    existingThreadNames.add(normalizedNpcName);
                }
            }

            event.getHook().editOriginal(getSuccessMessage(questChannel, createdThreads, skippedThreads)).queue();
        } catch (RuntimeException exception) {
            log.error("Failed creating quest {}", questName, exception);
            event.getHook().editOriginal(getFailureMessage(createdThreads, skippedThreads)).queue();
        }
    }

    private static List<String> parseNpcNames(String rawNpcNames) {
        String[] splitNpcNames = rawNpcNames.split(";");
        Set<String> seenNpcNames = new LinkedHashSet<>();
        List<String> npcNames = new ArrayList<>();

        for (String splitNpcName : splitNpcNames) {
            String npcName = splitNpcName.trim();
            if (npcName.isEmpty()) {
                continue;
            }

            String normalizedNpcName = npcName.toLowerCase(Locale.ROOT);
            if (seenNpcNames.add(normalizedNpcName)) {
                npcNames.add(npcName);
            }
        }

        return npcNames;
    }

    private static String validateInput(String questName, List<String> npcNames) {
        String channelName = Utils.getChannelName(questName);
        if (channelName.isBlank()) {
            return "Please provide a quest name that can be used as a Discord channel name.";
        }

        if (channelName.length() > DISCORD_CHANNEL_NAME_LIMIT) {
            return "The quest channel name is too long. Discord channel names can be at most 100 characters.";
        }

        if (npcNames.isEmpty()) {
            return "Please provide at least one NPC name, separated with semicolons.";
        }

        for (String npcName : npcNames) {
            if (npcName.length() > DISCORD_CHANNEL_NAME_LIMIT) {
                return "The NPC name \"" + npcName + "\" is too long. Discord thread names can be at most 100 characters.";
            }
        }

        return null;
    }

    private static String getQuestLockKey(Guild guild, String questName) {
        return guild.getId() + ":" + Utils.getChannelName(questName).toLowerCase(Locale.ROOT);
    }

    private static TextChannel getOrCreateQuestChannel(Guild guild, Category category, String questName) {
        String channelName = Utils.getChannelName(questName);

        return category.getTextChannels()
                .stream()
                .filter(channel -> channel.getName().equalsIgnoreCase(channelName))
                .findFirst()
                .orElseGet(() -> guild.createTextChannel(channelName, category)
                        .setTopic("Recording collection for " + questName)
                        .complete());
    }

    private static Set<String> getExistingThreadNames(TextChannel questChannel) {
        Set<String> threadNames = new LinkedHashSet<>();

        questChannel.getThreadChannels()
                .forEach(thread -> threadNames.add(thread.getName().toLowerCase(Locale.ROOT)));
        questChannel.retrieveArchivedPublicThreadChannels()
                .complete()
                .forEach(thread -> threadNames.add(thread.getName().toLowerCase(Locale.ROOT)));
        questChannel.retrieveArchivedPrivateThreadChannels()
                .complete()
                .forEach(thread -> threadNames.add(thread.getName().toLowerCase(Locale.ROOT)));

        return threadNames;
    }

    private static String getSubmissionMessage(Guild guild, TextChannel questChannel, String questName, String npcName) {
        return "# IMPORTANT PLEASE READ"
                + "\nCongratulations on getting the role! By voicing this character, you agree to the terms in " + Objects.requireNonNull(guild.getTextChannelById(820027818799792129L)).getAsMention() + "."
                + "\n\n### Submission Directions"
                + "\nPlease send in the final recording for " + npcName + " in one .wav file. If it is too large, rename it to " + npcName + ".wav, upload it to https://voicesofwynn.com/submit, and let us know in this thread."
                + "\nIf your character is in multiple scripts, each script's lines should be in a separate file and be named " + questName + "-" + npcName + ".wav."
                + "\n\nIf you wish, you may ping <@&" + VOICE_GUIDE_ROLE_ID + "> for feedback. This is not required."
                + "\n\nOnce your lines are checked, your voice actor role will be updated on Discord and on our website. If you don't have a contributor account yet, it will be created."
                + "\n\n### Deadline is one week."
                + "\nArrangements can be made if this is not possible but please communicate with us. If you have multiple scripts to finish, you may finish one or more scripts per week."
                + "\n\n### Script"
                + "\nPronunciation guide and script are in " + questChannel.getAsMention() + ". We will let you know if you have multiple scripts or any other special directions.";
    }

    private static String getSuccessMessage(TextChannel questChannel, List<String> createdThreads, List<String> skippedThreads) {
        StringBuilder message = new StringBuilder("Quest setup ready in ")
                .append(questChannel.getAsMention())
                .append(". Created ")
                .append(createdThreads.size())
                .append(" thread")
                .append(createdThreads.size() == 1 ? "" : "s")
                .append(".");

        if (!skippedThreads.isEmpty()) {
            message.append(" Skipped existing thread")
                    .append(skippedThreads.size() == 1 ? ": " : "s: ")
                    .append(String.join(", ", skippedThreads))
                    .append(".");
        }

        return message.toString();
    }

    private static String getFailureMessage(List<String> createdThreads, List<String> skippedThreads) {
        StringBuilder message = new StringBuilder("Failed to finish the quest setup. Check the bot logs for details.");

        if (!createdThreads.isEmpty()) {
            message.append(" Created before failing: ")
                    .append(String.join(", ", createdThreads))
                    .append(".");
        }

        if (!skippedThreads.isEmpty()) {
            message.append(" Already existed: ")
                    .append(String.join(", ", skippedThreads))
                    .append(".");
        }

        return message.toString();
    }
}
