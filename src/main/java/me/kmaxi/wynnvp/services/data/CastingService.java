package me.kmaxi.wynnvp.services.data;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Hands a Discord casting over to the website, where staff listen and vote. The API converts the
 * audio and keeps everything idempotent, so a re-run only adds threads it has not seen yet.
 */
@Service
@Slf4j
public class CastingService {

    private final APIKeys apiKeys;
    private final RestTemplate restTemplate;

    @Autowired
    public CastingService(APIKeys apiKeys) {
        this(apiKeys, new RestTemplate());
    }

    CastingService(APIKeys apiKeys, RestTemplate restTemplate) {
        this.apiKeys = apiKeys;
        this.restTemplate = restTemplate;
    }

    public record CastingRound(int roundId, boolean created, String adminUrl) {
    }

    public record UploadedAudition(int auditionId, boolean created) {
    }

    /**
     * Finds the quest's draft/open round on the website, or creates a draft one, and makes sure
     * every character exists in it.
     */
    public CastingRound ensureRound(String questName, List<String> characters) {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> body = Map.of("questName", questName, "characters", characters);

        ResponseEntity<CastingRound> response = restTemplate.postForEntity(
                Config.URL_BOT_CASTING + "/rounds",
                new HttpEntity<>(body, headers),
                CastingRound.class);

        CastingRound round = response.getBody();
        if (round == null) {
            throw new IllegalStateException("The API returned no casting round for " + questName);
        }
        return round;
    }

    /**
     * Uploads one audition thread's audio. Sending the same thread again is a no-op on the API side.
     */
    public UploadedAudition uploadAudition(int roundId,
                                           String characterName,
                                           String auditioneeName,
                                           String discordUserId,
                                           String discordThreadId,
                                           byte[] audio,
                                           String fileName) {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add("characterName", characterName);
        form.add("auditioneeName", auditioneeName);
        if (discordUserId != null) {
            form.add("discordUserId", discordUserId);
        }
        form.add("discordThreadId", discordThreadId);
        form.add("file", new ByteArrayResource(audio) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });

        ResponseEntity<UploadedAudition> response = restTemplate.postForEntity(
                Config.URL_BOT_CASTING + "/rounds/" + roundId + "/auditions",
                new HttpEntity<>(form, headers),
                UploadedAudition.class);

        UploadedAudition uploaded = response.getBody();
        if (uploaded == null) {
            throw new IllegalStateException("The API returned no audition for thread " + discordThreadId);
        }
        return uploaded;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKeys.botApiKey);
        return headers;
    }
}
