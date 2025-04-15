package me.kmaxi.wynnvp.services.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.dtos.LineReportDTO;
import me.kmaxi.wynnvp.enums.LineType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service @Slf4j
public class LineReportService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final APIKeys apiKeys;

    public LineReportService(APIKeys apiKeys) {
        this.apiKeys = apiKeys;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public List<LineReportDTO> fetchMessages(LineType type, String npcName) {
        try {
            String url = getReadingUrl(type, npcName);
            String response = restTemplate.getForObject(url, String.class);

            return objectMapper.readValue(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error fetching messages: ", e);
            return List.of(); // Return an empty list in case of an error
        }
    }

    public boolean resetForwarded() {
        try {
            String url = "https://voicesofwynn.com/api/unvoiced-line-report/reset";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/x-www-form-urlencoded");

            String data = "apiKey=" + apiKeys.updateApiKey;
            HttpEntity<String> entity = new HttpEntity<>(data, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
            return !response.getStatusCode().isError();
        } catch (Exception e) {
            log.error("Error resetting forwarded lines: ", e);
            return false;
        }
    }

    public List<LineReportDTO> getNewReports() {
        try {
            String url = "http://voicesofwynn.com/api/unvoiced-line-report/index?apiKey=" + apiKeys.readingApiKey;
            String response = restTemplate.getForObject(url, String.class);

            return objectMapper.readValue(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error fetching new reports: ", e);
            return List.of(); // Return an empty list in case of an error
        }
    }

    private String getReadingUrl(LineType type, String npcName) {
        return "https://voicesofwynn.com/api/unvoiced-line-report/" + type.getApiKeyword() + "?npc="
                + npcName.replace(" ", "%20") + "&apiKey=" + apiKeys.readingApiKey;
    }
}