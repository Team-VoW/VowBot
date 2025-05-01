package me.kmaxi.wynnvp.services.audition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.GuildService;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditionsChannelHandler {

    private final GuildService guildService;

    private final AuditionThreadHandler auditionThreadHandler;

    private static final int FETCH_MESSAGE_AMOUNT = 200;

    public void openAudition(String questName, String npcName, Member member) {

        log.info("Opening audition for {} in {} for {}", npcName, questName, member.getUser().getName());

        TextChannel questChannel = getQuestChannel(questName);
        if (questChannel == null) {
            log.error("Failed opening audition in {} because could not find audition channel", questName);
            Utils.sendPrivateMessage(member.getUser(), "ERROR OPENING AUDITION! No channel was found. Please contact a staff member or kmaxi about this!!");
            return;
        }

        User user = member.getUser();
        Optional<ThreadChannel> optionalThreadChannel = auditionThreadHandler.createThreadIfNotExists(user, npcName, questName, questChannel);

        if (optionalThreadChannel.isEmpty()){
            return;
        }
        ThreadChannel threadChannel = optionalThreadChannel.get();
        Message message = getFirstNotFullMessage(questChannel, npcName);

        if (message == null) {
            threadChannel.sendMessage("@everyone No audition lines were found for " + npcName + ".").queue();
            return;
        }

        //Edit the message. If the message just contains a . remove it. If it contains anything else do not remove anything but simply add to it.
        //The new content should be "\n--<link to thread channel>"
        String newContent = message.getContentRaw();
        if (newContent.length() == 1){
            newContent = "";
        } else {
            newContent  += "\n";
        }

        newContent += "- " + threadChannel.getAsMention();

        message.editMessage(newContent).queue();
    }


    public TextChannel getQuestChannel(String questName) {

        String channelName = getChannelName(questName);
        Guild guild = guildService.getGuild();
        List<TextChannel> channels = Objects.requireNonNull(guild.getCategoryById(Config.APPLY_CATEGORY_ID)).getTextChannels();

        return channels.stream()
                .filter(channel -> channel.getName().equalsIgnoreCase(channelName))
                .findFirst().orElse(null);

    }

    private String getChannelName(String questName) {
        return questName.replaceAll("[^a-zA-Z0-9-]", "");
    }

    public void createQuestChannels(String questName, List<String> roles) {

        TextChannel channel = createAuditionChannel(questName);

        for (String role : roles) {
            channel.sendMessage("# " + role).queue();

            //We send five messages as placeholder which can later be populated
            //With the audition links. This because discords max message size limit
            for (int i = 0; i < 3; i++){
                channel.sendMessage(".").queue();
            }
        }
    }

    private TextChannel createAuditionChannel(String questName){
        Guild guild = guildService.getGuild();
        Category category = guild.getCategoryById(Config.APPLY_CATEGORY_ID);

        String channelName = getChannelName(questName);
        return guild.createTextChannel(channelName, category)
                .setTopic("Recording collection for " + questName)
                .complete();
    }

    /**
     * Gets the first message that is sent in the channel after the npcName where
     * the message is not close to full, having space to put a link to a threadChannel
     * @param channel The TextChannel to look through
     * @param npcName The name of the npc to look for
     * @return The first message that is not full
     */
    private Message getFirstNotFullMessage(TextChannel channel, String npcName) {

        //Fetch this channels latest 200 messages. There should not be more than 200 messages
        List<Message> messageList = Utils.getMessageHistory(channel, FETCH_MESSAGE_AMOUNT);

        String currentCharacter = "";
        for (Message message : messageList) {
            String rawContent = message.getContentRaw();
            if (rawContent.startsWith("#")){
                //Set the current character to the first line of this message, removing everything after a \n
                currentCharacter = rawContent.split("\n")[0].replace("#", "").trim();
            }

            if(!currentCharacter.equals(npcName) || !message.getAuthor().isBot()){
                continue;
            }

            // Check if the message is not close to full (Discord's max message size is 2000 characters)
            // But we limit it a little earlier since the npc name + link takes a little under 100 characters
            if (rawContent.length() < 1900) {
                return message;
            }
        }

        // If no suitable message is found, return null
        return null;
    }

    public Map<String, List<ThreadChannel>> getNpcThreadMap(TextChannel channel) {
        List<Message> messageList = Utils.getMessageHistory(channel, 200);

        Map<String, List<ThreadChannel>> npcThreadsMap = new LinkedHashMap<>();
        String currentNpc = null;
        Pattern threadMentionPattern = Pattern.compile("<#(\\d+)>");

        for (Message message : messageList) {
            String content = message.getContentRaw();

            if (content.startsWith("#")) {
                currentNpc = content.split("\n")[0].replace("#", "").trim();
                npcThreadsMap.putIfAbsent(currentNpc, new ArrayList<>());
            }

            if (currentNpc != null && message.getAuthor().isBot()) {
                Matcher matcher = threadMentionPattern.matcher(content);
                while (matcher.find()) {
                    String threadId = matcher.group(1);
                    ThreadChannel thread = channel.getGuild().getThreadChannelById(threadId);
                    if (thread != null) {
                        npcThreadsMap.get(currentNpc).add(thread);
                    }
                }
            }
        }

        return npcThreadsMap;
    }

}
