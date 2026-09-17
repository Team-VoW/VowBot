package me.kmaxi.wynnvp.services.data;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.dtos.LineQueryResponseDTO;
import me.kmaxi.wynnvp.dtos.LineReportDTO;
import me.kmaxi.wynnvp.dtos.VowDialogueDTO;
import me.kmaxi.wynnvp.enums.LineType;
import me.kmaxi.wynnvp.enums.SetLinesCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class LineReportService {

    /** The API accepts up to 2000 messages per call; staying well under that keeps requests small. */
    private static final int CHUNK_SIZE = 500;

    private static final int MAX_LINES_PER_QUERY = 2000;

    private final RestTemplate restTemplate;
    private final APIKeys apiKeys;

    @Autowired
    public LineReportService(APIKeys apiKeys) {
        this(apiKeys, new RestTemplate());
    }

    LineReportService(APIKeys apiKeys, RestTemplate restTemplate) {
        this.apiKeys = apiKeys;
        this.restTemplate = restTemplate;
    }

    public List<LineReportDTO> fetchMessages(LineType type, String npcName) {
        // UriComponentsBuilder encodes the NPC name; the old code only replaced spaces, so a name
        // holding an '&' silently truncated the query.
        UriComponentsBuilder url = UriComponentsBuilder.fromUriString(Config.URL_BOT_REPORTS + "/lines")
                .queryParam("npc", npcName)
                .queryParam("limit", MAX_LINES_PER_QUERY);
        type.getStatuses().forEach(status -> url.queryParam("statuses", status));

        try {
            LineQueryResponseDTO response = restTemplate
                    .exchange(
                            url.encode().toUriString(),
                            HttpMethod.GET,
                            new HttpEntity<>(createHeaders()),
                            LineQueryResponseDTO.class)
                    .getBody();

            return response == null ? List.of() : response.getResults();
        } catch (Exception e) {
            log.error("Error fetching messages: ", e);
            return List.of(); // Return an empty list in case of an error
        }
    }

    public boolean setLinesAsVoiced(List<VowDialogueDTO> lines, SetLinesCommand command) {
        boolean allSuccess = true;
        for (int i = 0; i < lines.size(); i += CHUNK_SIZE) {
            List<String> chunk = lines.subList(i, Math.min(i + CHUNK_SIZE, lines.size())).stream()
                    .map(VowDialogueDTO::getLine)
                    .toList();
            if (!sendChunk(chunk, command)) {
                allSuccess = false;
            }
        }
        return allSuccess;
    }

    private boolean sendChunk(List<String> chatMessages, SetLinesCommand command) {
        String url = Config.URL_BOT_REPORTS + "/lines" + (command.isDelete() ? "" : "/status");
        Map<String, Object> body = command.isDelete()
                ? Map.of("chatMessages", chatMessages)
                : Map.of("chatMessages", chatMessages, "status", command.getApiStatus());

        try {
            restTemplate.exchange(
                    url,
                    command.isDelete() ? HttpMethod.DELETE : HttpMethod.POST,
                    new HttpEntity<>(body, createHeaders()),
                    String.class);
            return true;
        } catch (Exception e) {
            log.error("Error setting the status of {} lines: ", chatMessages.size(), e);
            return false;
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKeys.botApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
