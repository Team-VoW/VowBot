package me.kmaxi.wynnvp.services.audition;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.interfaces.StringIntInterface;
import me.kmaxi.wynnvp.services.GuildService;
import me.kmaxi.wynnvp.services.MemberHandler;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuditionsHandler {

    private final MemberHandler memberHandler;

    private final GuildService guildService;

    private final AuditionsChannelHandler auditionsChannelHandler;

    public void setupPoll(String questName, List<String> npcs, MessageChannel channel) {
        ArrayList<Character> reactions = new ArrayList<>();
        StringBuilder out = new StringBuilder(">>> **React to apply for a role in " + questName + "**");
        int i = 1; //1 for A, 26 for Z
        for (String npc : npcs) {
            if (i == 27) {
                break;
            }
            out.append("\n:").append(Utils.convertLetter(i)).append(": = ").append(npc).append("\n");
            reactions.add((char) (64 + i));
            i++;
        }

        channel.sendMessage(out.toString()).queue(message1 -> {
            for (char reaction : reactions) {
                message1.addReaction(Emoji.fromUnicode(Utils.getUnicode(reaction))).queue();
            }
        });

        auditionsChannelHandler.createQuestChannels(questName, npcs);

    }

    public CompletableFuture<String> finishedRole(Member member, Guild guild) {
        log.info("Finished role for {}", member.getEffectiveName());
        CompletableFuture<Void> completableFuture = memberHandler.upgradeActorRole(member, guild);

        // Role was not upgraded because person is already at highest role
        if (completableFuture == null) {
            return CompletableFuture.completedFuture("Thanks a lot for voicing this character " + member.getAsMention() + ":heart:. " +
                    "\nBecause you already are expert actor your role stayed the same this time :grin:");
        }

        return completableFuture.thenComposeAsync(v -> {
            CompletableFuture<String> resultFuture = new CompletableFuture<>();
            guild.retrieveMemberById(member.getId()).queueAfter(1, TimeUnit.SECONDS, updatedMember -> {

                String password = memberHandler.createAccount(member);

                if (password.isEmpty()) {
                    resultFuture.complete("Thanks a lot for voicing this character " + member.getAsMention() + ":heart:. " +
                            "\nYour actor role has been upgraded here and on the Website :partying_face:");
                } else {
                    resultFuture.complete("Thanks a lot for voicing your very first character for us " + member.getAsMention() + ":heart::partying_face:." +
                            "\n\n An account with the name " + member.getUser().getName() + " and the temporary password ||" +
                            password + "|| has been created for you on our website https://voicesofwynn.com/ " +
                            "\n\n Once everyone voice actor from this quest has sent in their lines, everything will be " +
                            " added to all the voice actors accounts. Feel free to go in there and change your bio, profile picture and we appreciate if you could fill out your **e-mail** so we can contact you easily if needed (feel free to make it private)! :grin:");
                }

            });
            return resultFuture;
        });
    }


    public String setRole(String questName, String npcName, User user) {
        Message message = getCastingMessage(questName, npcName);

        if (message == null) {
            return "Could not find quest " + questName + " or npc name " + npcName;
        }

        replaceLineWhereNpcIs(message, npcName, questName, ((number, line) -> {
            message.clearReactions(Emoji.fromUnicode(Utils.getUnicode((char) (number + 64)))).queue();
            line = line.replace(Utils.convertLetter(number), "x");
            if (line.contains("(")) {
                String[] split = line.split("\\(");
                line = split[0].trim();
            }
            return line.replace(npcName, npcName + " (" + user.getAsMention() + ")");
        }));

        String errorInAuditionChannel = auditionsChannelHandler.castRole(questName, npcName, user);
        return Objects.requireNonNullElseGet(errorInAuditionChannel, () -> "Assigned role " + npcName + " in " + questName + " quest.");

    }

    public String openRole(String questName, String npcName) {
        Message message = getCastingMessage(questName, npcName);

        if (message == null) {
            return "Could not find quest " + questName + " or npc name " + npcName;
        }

        replaceLineWhereNpcIs(message, npcName, questName, ((lineNumber, lineBefore) -> {
            message.addReaction(Emoji.fromUnicode(Utils.getUnicode((char) (lineNumber + 64)))).queue();
            return ":" + Utils.convertLetter(lineNumber) + ": = " + npcName;
        }));

        return "Cleared role " + npcName + " in " + questName + " quest.";
    }


    private Message getCastingMessage(String quest, String npcName) {
        npcName = npcName.toLowerCase();
        for (Message message : Objects.requireNonNull(guildService.getGuild().getNewsChannelById(Config.VOICE_APPLY_CHANNEL_ID)).getHistoryFromBeginning(100).complete().getRetrievedHistory()) {
            if (isQuestMessage(message, quest, npcName)) {
                return message;
            }
        }
        return null;
    }

    private boolean isQuestMessage(Message message, String quest, String npcName){
        String messageAsString = message.getContentRaw();
        if (!message.getAuthor().isBot()) {
            return false;
        }
        String[] messageArray = messageAsString.split("\n");
        String questName = messageArray[0].replace("React to apply for a role in ", "");
        questName = questName.replace(">>>", "");
        questName = questName.replace("**", "");
        questName = questName.replace(" ", "");

        return questName.equalsIgnoreCase(quest) && messageAsString.toLowerCase().contains(npcName);
    }

    private void replaceLineWhereNpcIs(Message message, String npcName, String questName, StringIntInterface lineChange) {
        String[] messageArray = message.getContentRaw().split("\n");
        StringBuilder out = new StringBuilder(">>> **React to apply for a role in " + questName + "**");

        boolean hasChangedAline = false;
        for (int i = 0; i < messageArray.length; i++) {
            String line = messageArray[i];
            if (!line.contains(npcName) || hasChangedAline) {
                if (line.contains("to apply for")) {
                    continue;
                }
                out.append("\n").append(line);
                continue;
            }
            int number = (int) (((double) i / 2.0) + 0.5);
            message.addReaction(Emoji.fromUnicode(Utils.getUnicode(number))).queue();
            line = lineChange.operation((int) (((double) i / 2.0) + 0.5), line);
            out.append("\n").append(line);
            hasChangedAline = true;
        }
        message.editMessage(out.toString()).queue();
    }

}
