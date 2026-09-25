package me.kmaxi.wynnvp.services.data;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CastingServiceTest {

    private static final String API_KEY = "integration-secret";

    private MockRestServiceServer server;
    private CastingService castingService;

    @BeforeEach
    void setUp() {
        APIKeys apiKeys = new APIKeys();
        apiKeys.botApiKey = API_KEY;

        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        castingService = new CastingService(apiKeys, restTemplate);
    }

    @Test
    void ensureRoundSendsTheQuestAndItsCharacters() {
        server.expect(once(), requestTo(Config.URL_BOT_CASTING + "/rounds"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
                .andExpect(content().json("""
                        {"questName": "Ragni", "characters": ["Guard", "Mayor"]}
                        """))
                .andRespond(withSuccess("""
                        {"roundId": 7, "created": true, "adminUrl": "https://app.voicesofwynn.com/admin/casting/7"}
                        """, MediaType.APPLICATION_JSON));

        CastingService.CastingRound round = castingService.ensureRound("Ragni", List.of("Guard", "Mayor"));

        assertThat(round.roundId()).isEqualTo(7);
        assertThat(round.created()).isTrue();
        assertThat(round.adminUrl()).endsWith("/admin/casting/7");
        server.verify();
    }

    @Test
    void uploadAuditionPostsTheAudioAsMultipartKeyedByThread() {
        server.expect(once(), requestTo(Config.URL_BOT_CASTING + "/rounds/7/auditions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(request -> {
                    String body = request.getBody().toString();
                    assertThat(body).contains("name=\"characterName\"").contains("Guard");
                    assertThat(body).contains("name=\"discordThreadId\"").contains("123");
                    assertThat(body).contains("filename=\"take.ogg\"");
                })
                .andRespond(withSuccess("""
                        {"auditionId": 55, "created": false}
                        """, MediaType.APPLICATION_JSON));

        CastingService.UploadedAudition uploaded = castingService.uploadAudition(
                7, "Guard", "kmaxi", "999", "123", "audio".getBytes(StandardCharsets.UTF_8), "take.ogg");

        assertThat(uploaded.auditionId()).isEqualTo(55);
        assertThat(uploaded.created()).isFalse();
        server.verify();
    }
}
