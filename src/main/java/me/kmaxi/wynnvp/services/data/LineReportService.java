package me.kmaxi.wynnvp.services.data;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.dtos.LineReportData;
import me.kmaxi.wynnvp.enums.LineType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private static String getReadingUrl(LineType type, String npcName) {
        return "https://voicesofwynn.com/api/unvoiced-line-report/" + type.getApiKeyword() + "?npc="
                + npcName.replace(" ", "%20") + "&apiKey=" + APIKeys.readingApiKey;
    }
}
