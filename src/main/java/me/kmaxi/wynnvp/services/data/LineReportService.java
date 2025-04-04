package me.kmaxi.wynnvp.services.data;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.dtos.LineReportData;
import me.kmaxi.wynnvp.enums.LineType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


@Service
public class LineReportService {

    private final RestTemplate restTemplate;

    public LineReportService() {
        this.restTemplate = new RestTemplate();
    }

    public List<LineReportData> fetchMessages(LineType type, String npcName) {
        String url = getReadingUrl(type, npcName);
        String response = restTemplate.getForObject(url, String.class);
        JSONArray jsonArray = new JSONArray(response);
        List<LineReportData> messages = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            LineReportData messageData = new LineReportData(
                    jsonObject.getString("message"),
                    jsonObject.getString("NPC"),
                    jsonObject.getString("X"),
                    jsonObject.getString("Y"),
                    jsonObject.getString("Z")
            );
            messages.add(messageData);
        }
        return messages;
    }

    public boolean resetForwarded() {
        try {
            String url = "https://voicesofwynn.com/api/unvoiced-line-report/reset";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            String data = "apiKey=" + APIKeys.updateApiKey;
            HttpEntity<String> entity = new HttpEntity<>(data, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            boolean isError = response.getStatusCode().isError();

            return !isError;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getReadingUrl(LineType type, String npcName) {
        return "https://voicesofwynn.com/api/unvoiced-line-report/" + type.getApiKeyword() + "?npc="
                + npcName.replace(" ", "%20") + "&apiKey=" + APIKeys.readingApiKey;
    }
}
