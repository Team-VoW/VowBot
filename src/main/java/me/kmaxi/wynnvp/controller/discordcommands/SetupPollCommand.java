package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Component
public class SetupPollCommand implements ICommandImpl {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("setuppoll", "Sets up the voting poll from a casting club call casting")
                .addOptions(new OptionData(OptionType.STRING, "url", "The url to the casting call", true));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        log.info("Set up poll command executed by {}", event.getUser().getName());
        event.deferReply().setEphemeral(true).queue();

        String URL = Objects.requireNonNull(event.getOption("url")).getAsString(); // The URL of the project will be provided in the command

        event.getChannel().sendMessage("Auditions for " + URL).queue();
        try {
            Document doc = Jsoup.connect(URL).get();

            String castingCallTitle = doc.title();
            ArrayList<String> roleIds = getIDS(doc);


            roleIds.forEach(log::info);

            log.info("Title from Casting Call Club: {}", castingCallTitle);


            //For each role
            for (String roleId : roleIds) {
                ArrayList<JSONObject> auditions = getAuditions(roleId);

                String roleName = auditions.get(0).getString("roleName").trim().replaceAll("[ ,.-]", "_");

                //PollSQL.createPoll(roleName);


                // Create a thread for each role
                final String threadName = roleName + " Auditions";
                Message startMessage = event.getChannel().sendMessage("Auditions for " + roleName).complete();
                ThreadChannel threadChannelAction = startMessage.createThreadChannel(threadName).complete();

                sendAudioFiles(auditions, threadChannelAction);
            }


        } catch (IOException e) {
            log.error("Error while trying to connect to the URL: {}", URL, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        event.getHook().editOriginal("Finished sending everything").queue();
    }


    private void sendAudioFiles(ArrayList<JSONObject> auditions, MessageChannel channel) {
        for (int j = 0; j < auditions.size(); j++) {


            JSONObject audition = auditions.get(j);
            String audioURL = audition.getString("audioUrl");
            String userName = audition.getString("username");
            String audioFileName = userName + ".mp3";
            String roleName = audition.getString("roleName").trim().replaceAll("[ ,.-]", "_");

            try {
                URL website = new URL(audioURL);
                try (ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                     FileOutputStream fos = new FileOutputStream(audioFileName)) {
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                }
                File file = new File(audioFileName);

                String messageText = roleName + " " + (j + 1) + " " + userName;

                Button voteButton = Button.primary(
                        messageText.replace(" ", "-") + "-" + Config.voteButtonLabel, Config.voteButtonLabel);
                Button removeVoteButton = Button.danger(
                        messageText.replace(" ", "-") + "-" + Config.removeVoteButtonLabel, Config.removeVoteButtonLabel);
                ActionRow.of(voteButton, removeVoteButton);
                channel.sendMessage("```" + messageText + "```").addActionRow(voteButton, removeVoteButton).addFiles(FileUpload.fromData(file)).queue();

                if (!file.delete()) {
                    log.warn("Failed to delete file: {}", file.getAbsolutePath());
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private ArrayList<JSONObject> getAuditions(String roleId) {
        ArrayList<JSONObject> auditions = new ArrayList<>(); //Array of objects

        JSONArray lastJsonArray = null;
        int page = 1;
        while (true) {

            JSONObject jsonData;
            try {
                String url = "https://www.castingcall.club/api/v3/roles/" + roleId + "/submissions?page=" + page;
                log.info("Getting auditions from {}", url);
                jsonData = getJsonObject(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            JSONArray auditionsArray = jsonData.getJSONArray("submissions");

            if (auditionsArray.isEmpty() || (lastJsonArray != null && auditionsArray.toString().equals(lastJsonArray.toString())))
                break;

            lastJsonArray = auditionsArray;

            for (int j = 0; j < auditionsArray.length(); j++) {
                JSONObject audition = auditionsArray.getJSONObject(j);

                auditions.add(audition);


            }
            page++;
        }

        return auditions;
    }

    private ArrayList<String> getIDS(Document doc) {

        ArrayList<String> roleIds = new ArrayList<>();

        Elements images = doc.select("img[src]");

        for (Element image : images) {
            String src = image.attr("src");

            if (!src.contains("role"))
                continue;

            String result = src.substring(src.indexOf("attachment/") + "attachment/".length(), src.indexOf("/", src.indexOf("attachment/") + "attachment/".length()));

            //If result is not made up of 7 numbers, continue
            if (!result.matches("\\d{7}"))
                continue;

            roleIds.add(result);
        }

        return roleIds;
    }

    private JSONObject getJsonObject(String urlToRead) throws Exception {
        URL url = new URL(urlToRead);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set the User-Agent header
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return new JSONObject(response.toString());
        } finally {
            connection.disconnect();
        }
    }

}
