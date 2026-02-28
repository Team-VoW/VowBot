package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.AudioConversionService;
import me.kmaxi.wynnvp.services.DiscordPollHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
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
import java.nio.file.Files;
import java.util.*;

@Slf4j
@Component
public class SetupPollCommand implements ICommandImpl {

    private final DiscordPollHandler pollHandler;
    private final APIKeys apiKeys;
    private final AudioConversionService audioConversionService;

    public SetupPollCommand(DiscordPollHandler pollHandler, APIKeys apiKeys, AudioConversionService audioConversionService) {
        this.pollHandler = pollHandler;
        this.apiKeys = apiKeys;
        this.audioConversionService = audioConversionService;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("setuppoll", "Sets up the voting poll from a casting club call casting")
                .addOptions(new OptionData(OptionType.STRING, "url", "The url to the casting call", false))
                .addOptions(new OptionData(OptionType.STRING, "quest", "The quest name if setting up a poll from discord auditions", false));
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        log.info("Set up poll command executed by {}", event.getUser().getName());
        event.deferReply().setEphemeral(true).queue();

        OptionMapping questName = event.getOption("quest");
        if (questName != null) {
            String quest = questName.getAsString();
            event.getHook().editOriginal(pollHandler.setupPoll(quest)).queue();
            return;
        }

        if (event.getOption("url") == null) {
            event.getHook().editOriginal("Please provide a URL or QuestName").queue();
            return;
        }

        String url = Objects.requireNonNull(event.getOption("url")).getAsString();

        event.getChannel().sendMessage("Auditions for " + url).queue();
        try {
            Document doc = Jsoup.connect(url).get();
            String castingCallTitle = doc.title();
            log.info("Title from Casting Call Club: {}", castingCallTitle);

            String projectId = getProjectId(doc);
            log.info("Found project ID: {}", projectId);

            ArrayList<JSONObject> allSubmissions = getAllSubmissions(projectId);
            log.info("Total submissions fetched: {}", allSubmissions.size());

            // Group by role name
            Map<String, List<JSONObject>> byRole = new LinkedHashMap<>();
            for (JSONObject sub : allSubmissions) {
                String roleName = sub.getString("roleName").trim();
                byRole.computeIfAbsent(roleName, k -> new ArrayList<>()).add(sub);
            }

            for (Map.Entry<String, List<JSONObject>> entry : byRole.entrySet()) {
                String roleName = entry.getKey().replaceAll("[ ,.-]", "_");
                String threadName = roleName + " Auditions";
                Message startMessage = event.getChannel().sendMessage("Auditions for " + entry.getKey()).complete();
                ThreadChannel threadChannelAction = startMessage.createThreadChannel(threadName).complete();
                sendAudioFiles(new ArrayList<>(entry.getValue()), threadChannelAction);
            }

        } catch (IOException e) {
            log.error("Error while trying to connect to the URL: {}", url, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        event.getHook().editOriginal("Finished sending everything").queue();
    }

    private String getProjectId(Document doc) {
        Elements links = doc.select("a[href*=project_id=]");
        for (Element link : links) {
            String href = link.attr("href");
            String[] parts = href.split("[?&]");
            for (String part : parts) {
                if (part.startsWith("project_id=")) {
                    return part.substring("project_id=".length());
                }
            }
        }
        throw new RuntimeException("Could not find project_id in page");
    }

    private ArrayList<JSONObject> getAllSubmissions(String projectId) throws Exception {
        // Step 1: establish session by hitting the home page with _ccc_token
        String cccToken = apiKeys.cccToken;
        String sessionCookie = fetchSessionCookie(cccToken);
        String cookieHeader = "_ccc_token=" + cccToken + "; _ccc_session=" + sessionCookie;

        ArrayList<JSONObject> submissions = new ArrayList<>();
        JSONArray lastJsonArray = null;
        int page = 1;

        while (true) {
            String apiUrl = "https://www.castingcall.club/api/v3/manage/projects/" + projectId
                    + "/submissions?order_by=updated_at&review_status=unsorted&page=" + page;
            log.info("Fetching submissions from {}", apiUrl);

            JSONObject jsonData = getJsonObject(apiUrl, cookieHeader);

            // Log the first submission on first page so we can verify field names
            if (page == 1) {
                JSONArray arr = jsonData.optJSONArray("submissions");
                if (arr != null && !arr.isEmpty()) {
                    log.info("Raw first submission JSON: {}", arr.getJSONObject(0));
                }
            }

            JSONArray auditionsArray = jsonData.getJSONArray("submissions");

            if (auditionsArray.isEmpty()
                    || (lastJsonArray != null && auditionsArray.toString().equals(lastJsonArray.toString()))) {
                break;
            }

            lastJsonArray = auditionsArray;

            for (int j = 0; j < auditionsArray.length(); j++) {
                submissions.add(auditionsArray.getJSONObject(j));
            }
            page++;
        }

        return submissions;
    }

    /**
     * GETs the CCC home page with the persistent token and extracts _ccc_session from Set-Cookie.
     */
    private String fetchSessionCookie(String cccToken) throws Exception {
        URL url = new URL("https://www.castingcall.club/");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Cookie", "_ccc_token=" + cccToken);

        // Consume response so headers are populated
        connection.getResponseCode();

        String sessionValue = null;
        for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
            if ("Set-Cookie".equalsIgnoreCase(header.getKey())) {
                for (String cookieStr : header.getValue()) {
                    if (cookieStr.startsWith("_ccc_session=")) {
                        sessionValue = cookieStr.split(";")[0].substring("_ccc_session=".length());
                        break;
                    }
                }
            }
            if (sessionValue != null) break;
        }

        connection.disconnect();

        if (sessionValue == null) {
            throw new RuntimeException("Could not obtain _ccc_session cookie from CCC home page");
        }
        log.info("Obtained _ccc_session cookie");
        return sessionValue;
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

                // Convert/compress via FFmpeg (no-op if already MP3 < 7MB, deletes original if conversion runs)
                try {
                    file = audioConversionService.convertToMp3(file);
                } catch (IOException e) {
                    log.warn("Audio conversion failed for {}, skipping: {}", audioFileName, e.getMessage());
                    Files.deleteIfExists(new File(audioFileName).toPath());
                    continue;
                }

                String messageText = roleName + " " + (j + 1) + " " + userName;
                final File finalFile = file;

                channel.sendMessage("```" + messageText + "```")
                        .addFiles(FileUpload.fromData(finalFile))
                        .queue(
                                success -> {
                                    success.addReaction(Emoji.fromUnicode(Config.ACCEPT_UNICODE)).queue();
                                    try {
                                        Files.deleteIfExists(finalFile.toPath());
                                    } catch (IOException e) {
                                        log.warn("Failed to delete file: {}", finalFile.getAbsolutePath(), e);
                                    }
                                },
                                failure -> log.error("Failed to send audio file: {}", finalFile.getName(), failure)
                        );

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private JSONObject getJsonObject(String urlToRead, String cookieHeader) throws Exception {
        URL url = new URL(urlToRead);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Cookie", cookieHeader);
        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        connection.setRequestProperty("Accept", "application/json");

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
