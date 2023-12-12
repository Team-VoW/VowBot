package me.kmaxi.wynnvp.slashcommands.poll;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.ThreadChannelAction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SetUpPollCommand {

    public static void SetUpPoll(SlashCommandInteractionEvent event) {

        event.deferReply().setEphemeral(true).queue();

        String URL = event.getOption("url").getAsString(); // The URL of the project will be provided in the command

        event.getChannel().sendMessage("Auditions for " + URL).queue();
        try {
            Document doc = Jsoup.connect(URL).get();

            String castingCallTitle = doc.title();
            ArrayList<String> roleIds = getIDS(doc);
            ArrayList<String> roleNames = getRoleNames(doc);

            roleNames.forEach(System.out::println);
            roleIds.forEach(System.out::println);

            System.out.println(castingCallTitle);


            //For each role
            for (int i = 0; i < roleIds.size(); i++) {
                String roleId = roleIds.get(i);
                String roleName = roleNames.get(i);
                roleName = roleName.trim().replaceAll("[ ,.-]", "_");

                PollSQL.createPoll(roleName);

                ArrayList<JSONObject> auditions = getAuditions(roleId);

                // Create a thread for each role
                final String threadName = roleName + " Auditions";
                Message startMessage = event.getTextChannel().sendMessage("Auditions for " + roleName).complete();
                ThreadChannel threadChannelAction = startMessage.createThreadChannel(threadName).complete();

                sendAudioFiles(auditions, threadChannelAction);
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        event.getHook().editOriginal("Finished sending everything").queue();
    }


    private static void sendAudioFiles(ArrayList<JSONObject> auditions, MessageChannel channel) {
        for (int j = 0; j < auditions.size(); j++) {


            JSONObject audition = auditions.get(j);
            String audioURL = audition.getString("public_audio_url");
            String userName = audition.getString("username");
            String audioFileName = userName + ".mp3";
            String roleName = audition.getString("role_name").trim().replaceAll("[ ,.-]", "_");

            try {
                URL website = new URL(audioURL);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(audioFileName);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                File file = new File(audioFileName);

                String messageText = roleName + " " + (j + 1) + " " + userName;

                Button voteButton = Button.primary(
                        messageText.replace(" ", "-") + "-" + Config.voteButtonLabel, Config.voteButtonLabel);
                Button removeVoteButton = Button.danger(
                        messageText.replace(" ", "-") + "-" + Config.removeVoteButtonLabel, Config.removeVoteButtonLabel);
                ActionRow row = ActionRow.of(voteButton, removeVoteButton);
                channel.sendMessage("```" +  messageText + "```").setActionRows(row).addFile(file).queue();

                file.delete();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static ArrayList<JSONObject> getAuditions(String roleId) {
        ArrayList<JSONObject> auditions = new ArrayList<>(); //Array of objects

        int page = 1;
        while (true) {

            JSONObject jsonData;
            try {
                jsonData = getJsonObject("https://www.castingcall.club/api/v1/roles/" + roleId + "/auditions?order_by=updated_at&status=all&page=" + page);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            JSONArray auditionsArray = jsonData.getJSONArray("auditions");

            if (auditionsArray.length() == 0)
                break;

            for (int j = 0; j < auditionsArray.length(); j++) {
                JSONObject audition = auditionsArray.getJSONObject(j);

                auditions.add(audition);


            }
            page++;
        }

        return auditions;
    }

    private static ArrayList<String> getRoleNames(Document doc) {
        ArrayList<String> names = new ArrayList<>();


        Elements roles = doc.select(".panel");

        for (Element role : roles) {

            String classAttr = role.attr("class");

            if (!classAttr.equals("panel panel-success")) {
                continue;
            }

            Element data = role.getElementsByClass("panel-heading").get(0);

            String namePattern = "<div[^>]*>([^<]*)<\\/div>";
            Pattern r = Pattern.compile(namePattern);
            Matcher m = r.matcher(data.toString());

            if (m.find()) {
                names.add(m.group(1).trim());
            }
        }
        return names;
    }

    private static ArrayList<String> getIDS(Document doc) {

        ArrayList<String> roleIds = new ArrayList<>();

        Elements images = doc.select("img[src]");

        for (Element image : images) {
            String src = image.attr("src");

            if (!src.contains("role"))
                continue;

            String result = src.substring(src.indexOf("attachment/") + "attachment/".length(), src.indexOf("/", src.indexOf("attachment/") + "attachment/".length()));

            roleIds.add(result);
        }

        return roleIds;
    }

    private static JSONObject getJsonObject(String urlToRead) throws Exception {
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
