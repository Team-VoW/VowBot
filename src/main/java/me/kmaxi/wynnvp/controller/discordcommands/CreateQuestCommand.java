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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component
public class CreateQuestCommand implements ICommandImpl {

    private static final int DISCORD_CHANNEL_NAME_LIMIT = 100;
    private static final String NAME_OPTION = "name";
    private static final String NPCS_OPTION = "npcs";

    private final Executor questSetupExecutor;

    public CreateQuestCommand(Executor questSetupExecutor) {
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

        questSetupExecutor.execute(() -> setupQuest(event, questName, npcNames));
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
            TextChannel questChannel = getOrCreateQuestChannel(guild, recordingCollectionCategory, questName);
            Set<String> existingThreadNames = getExistingThreadNames(questChannel);

            for (String npcName : npcNames) {
                String normalizedNpcName = npcName.toLowerCase(Locale.ROOT);
                if (existingThreadNames.contains(normalizedNpcName)) {
                    skippedThreads.add(npcName);
                    continue;
                }

                ThreadChannel threadChannel = questChannel.createThreadChannel(npcName)
                        .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_1_WEEK)
                        .complete();
                threadChannel.sendMessage(getSubmissionMessage(guild, questName, npcName)).complete();
                createdThreads.add(npcName);
                existingThreadNames.add(normalizedNpcName);
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

        return threadNames;
    }

    private static String getSubmissionMessage(Guild guild, String questName, String npcName) {
        return "Please send in the recordings for **" + npcName + "** in **one wav file** as soon as you are able to. "
                + "If your file exceeds Discord upload limit (or you don't want to upload to Discord), you can rename your file to `" + npcName + ".wav` and upload it to our filedrop at https://voicesofwynn.com/submit. Once you do, please send a message here to inform the cast manager of your submission. "
                + "Once every person that voices a character in **" + questName + "** has sent in their lines it will be added to the mod and website (https://voicesofwynn.com). "
                + "If this is your first role then you'll get login details for your account when it's uploaded."
                + "\n\nA staff member will send the script of this quest very soon."
                + "\n\nBy voicing this character, you agreed to the terms listed in " + Objects.requireNonNull(guild.getTextChannelById(820027818799792129L)).getAsMention();
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
