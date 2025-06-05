package me.kmaxi.wynnvp.services.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.dtos.LineReportDTO;
import me.kmaxi.wynnvp.dtos.VowDialogueDTO;
import me.kmaxi.wynnvp.enums.LineType;
import me.kmaxi.wynnvp.enums.SetLinesCommand;
import okhttp3.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
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

    public boolean setLinesAsVoiced(List<VowDialogueDTO> lines, SetLinesCommand command) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        // Fixed deprecated method - parameter order is reversed in new version
        RequestBody body = RequestBody.create("apiKey=testing&lines[]=[5/16] Lari: ⓐⓣⓗⓒⓗⓔⓐⓝⓖⓐⓘⓛ!&lines[]=[1/1] Aledar: soldier, you take the skeleton in the front. We'll handle the two zombies in the back!&answer=" + command.getShorthand(), mediaType);

        Request request = new Request.Builder()
                .url("http://127.0.0.1/api/unvoiced-line-report/resolve")
                .method("PUT", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Failed to set lines as voiced: {}", response.message());
                return false;
            }
            assert response.body() != null;
            String responseBody = response.body().string();
            log.info("Response from setting lines as voiced: {}", responseBody);
            return true;
        } catch (IOException e) {
            log.error("Error setting lines as voiced: ", e);
            return false;
        }
    }
}