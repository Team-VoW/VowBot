package me.kmaxi.wynnvp.slashcommands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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

    private static String castingCallURL = "https://www.castingcall.club/projects/voices-of-wynn-huge-community-voice-acting-project-hunger-of-the-gerts";

    public static void SetUpPoll(SlashCommandInteractionEvent event) {

        event.deferReply().queue();

        String URL = event.getOption("url").getAsString(); // The URL of the project will be provided in the command
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

                ArrayList<JSONObject> auditions = getAuditions(roleId);

                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\n").append("**ROLE: ").append(roleNames.get(i)).append("**");

                //For each audition
                for (int j = 0; j < auditions.size(); j++) {
                    JSONObject audition = auditions.get(j);

                    String username = audition.getString("username");
                    String audioURL = audition.getString("public_audio_url");

                    stringBuilder.append("\n").append(j).append(" ").append(username).append(" ").append(audioURL);
                }

                event.getTextChannel().sendMessage(stringBuilder).queue();

                System.out.println(stringBuilder);

                sendAudioFiles(auditions, event.getMessageChannel());
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
            String audioFileName = audition.getString("username") + ".mp3";

            try {

                URL website = new URL(audioURL);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(audioFileName);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                File file = new File(audioFileName);
                channel.sendFile(file).queue();
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

            JSONObject jsonData = null;
            try {
                jsonData = getJsonObject("https://www.castingcall.club/api/v1/roles/" + roleId + "/auditions?order_by=updated_at&status=all&page=" + page);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            //auditions.appendArray(jsonParsed.auditions) //Append the array of auditions

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
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return new JSONObject(result.toString());
    }

}





