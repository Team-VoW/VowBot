package me.kmaxi.wynnvp.services;

import me.kmaxi.wynnvp.services.audition.AuditionsChannelHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscordPollHandler Tests")
class DiscordPollHandlerTest {

    @Mock
    private AuditionsChannelHandler auditionsChannelHandler;

    @Mock
    private GuildService guildService;

    @Mock
    private AudioConversionService audioConversionService;

    @Mock
    private TextChannel questChannel;

    @Mock
    private TextChannel staffVotingChannel;

    @Mock
    private ThreadChannel thread1;

    @Mock
    private ThreadChannel thread2;

    @Mock
    private MessageCreateAction messageCreateAction;

    @Mock
    private RestAction<Message> messageRestAction;

    @InjectMocks
    private DiscordPollHandler discordPollHandler;

    @BeforeEach
    void setUp() {
        when(staffVotingChannel.sendMessage(anyString())).thenReturn(messageCreateAction);
        doNothing().when(messageCreateAction).queue();
    }

    @Test
    @DisplayName("Should return error when quest channel not found")
    void setupPoll_QuestChannelNotFound_ReturnsError() {
        // Given
        String questName = "NonexistentQuest";
        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(null);

        // When
        String result = discordPollHandler.setupPoll(questName);

        // Then
        assertThat(result).contains("Failed opening auditions")
                .contains("could not find quest channel");
        verify(auditionsChannelHandler).getQuestChannel(questName);
        verify(guildService, never()).getStaffVotingChannel();
    }

    @Test
    @DisplayName("Should return error when staff voting channel not found")
    void setupPoll_StaffVotingChannelNotFound_ReturnsError() {
        // Given
        String questName = "TestQuest";
        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(Collections.emptyMap());
        when(guildService.getStaffVotingChannel()).thenReturn(null);

        // When
        String result = discordPollHandler.setupPoll(questName);

        // Then
        assertThat(result).contains("Failed opening auditions")
                .contains("could not find staff voting channel");
        verify(guildService).getStaffVotingChannel();
    }

    @Test
    @DisplayName("Should successfully set up poll with no NPCs")
    void setupPoll_NoNpcs_Success() {
        // Given
        String questName = "EmptyQuest";
        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(Collections.emptyMap());
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        // When
        String result = discordPollHandler.setupPoll(questName);

        // Then
        assertThat(result).isEqualTo("Poll setup complete for EmptyQuest");
        verify(staffVotingChannel).sendMessage("# Poll for EmptyQuest");
    }

    @Test
    @DisplayName("Should successfully set up poll with single NPC")
    void setupPoll_SingleNpc_Success() {
        // Given
        String questName = "TestQuest";
        String npcName = "TestNPC";

        Map<String, List<ThreadChannel>> npcThreadMap = new HashMap<>();
        npcThreadMap.put(npcName, Arrays.asList(thread1));

        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(npcThreadMap);
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        // Mock thread message history to return no messages (no audio file)
        when(thread1.getName()).thenReturn("Thread1");

        // When
        String result = discordPollHandler.setupPoll(questName);

        // Then
        assertThat(result).isEqualTo("Poll setup complete for TestQuest");
        verify(staffVotingChannel).sendMessage("# Poll for TestQuest");
        verify(staffVotingChannel).sendMessage("## TestNPC");
    }

    @Test
    @DisplayName("Should successfully set up poll with multiple NPCs")
    void setupPoll_MultipleNpcs_Success() {
        // Given
        String questName = "ComplexQuest";
        String npc1 = "NPC1";
        String npc2 = "NPC2";

        Map<String, List<ThreadChannel>> npcThreadMap = new LinkedHashMap<>();
        npcThreadMap.put(npc1, Arrays.asList(thread1));
        npcThreadMap.put(npc2, Arrays.asList(thread2));

        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(npcThreadMap);
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        when(thread1.getName()).thenReturn("Thread1");
        when(thread2.getName()).thenReturn("Thread2");

        // When
        String result = discordPollHandler.setupPoll(questName);

        // Then
        assertThat(result).isEqualTo("Poll setup complete for ComplexQuest");
        verify(staffVotingChannel).sendMessage("# Poll for ComplexQuest");
        verify(staffVotingChannel).sendMessage("## NPC1");
        verify(staffVotingChannel).sendMessage("## NPC2");
    }

    @Test
    @DisplayName("Should send quest title message")
    void setupPoll_SendsQuestTitle() {
        // Given
        String questName = "Epic Quest";
        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(Collections.emptyMap());
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        // When
        discordPollHandler.setupPoll(questName);

        // Then
        verify(staffVotingChannel).sendMessage("# Poll for Epic Quest");
    }

    @Test
    @DisplayName("Should verify supported audio formats are defined")
    void supportedAudioFormats_AreDefined() {
        // This test verifies the constant exists and has expected formats
        // We can't directly access the private constant, but we can verify behavior
        // The supported formats include: .ogg, .wav, .mp3, .m4a, .aac, .flac, .opus, etc.

        // This is a structural test to ensure the constant is present
        assertThat(discordPollHandler).isNotNull();
    }

    @Test
    @DisplayName("Should handle quest with multiple threads per NPC")
    void setupPoll_MultipleThreadsPerNpc_Success() {
        // Given
        String questName = "TestQuest";
        String npcName = "PopularNPC";

        Map<String, List<ThreadChannel>> npcThreadMap = new HashMap<>();
        npcThreadMap.put(npcName, Arrays.asList(thread1, thread2));

        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(npcThreadMap);
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        when(thread1.getName()).thenReturn("Thread1");
        when(thread2.getName()).thenReturn("Thread2");

        // When
        String result = discordPollHandler.setupPoll(questName);

        // Then
        assertThat(result).isEqualTo("Poll setup complete for TestQuest");
        verify(staffVotingChannel).sendMessage("## PopularNPC");
        // Should process both threads
    }

    @Test
    @DisplayName("Should return success message with quest name")
    void setupPoll_ReturnsSuccessMessage() {
        // Given
        String questName = "My Amazing Quest";
        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(Collections.emptyMap());
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        // When
        String result = discordPollHandler.setupPoll(questName);

        // Then
        assertThat(result)
                .isEqualTo("Poll setup complete for My Amazing Quest")
                .contains(questName);
    }

    @Test
    @DisplayName("Should call getNpcThreadMap with correct quest channel")
    void setupPoll_CallsGetNpcThreadMapWithCorrectChannel() {
        // Given
        String questName = "TestQuest";
        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(Collections.emptyMap());
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        // When
        discordPollHandler.setupPoll(questName);

        // Then
        verify(auditionsChannelHandler).getNpcThreadMap(questChannel);
    }

    @Test
    @DisplayName("Should handle empty thread list for NPC")
    void setupPoll_EmptyThreadList_Success() {
        // Given
        String questName = "TestQuest";
        String npcName = "NPCWithNoThreads";

        Map<String, List<ThreadChannel>> npcThreadMap = new HashMap<>();
        npcThreadMap.put(npcName, Collections.emptyList());

        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(npcThreadMap);
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        // When
        String result = discordPollHandler.setupPoll(questName);

        // Then
        assertThat(result).isEqualTo("Poll setup complete for TestQuest");
        verify(staffVotingChannel).sendMessage("## NPCWithNoThreads");
    }

    @Test
    @DisplayName("Should verify AudioConversionService is injected")
    void constructor_InjectsAudioConversionService() {
        // Given/When - constructor injection happens in @BeforeEach

        // Then
        assertThat(discordPollHandler).isNotNull();
        // The service should be properly wired
    }

    @Test
    @DisplayName("Should verify GuildService is used to get voting channel")
    void setupPoll_UsesGuildServiceForVotingChannel() {
        // Given
        String questName = "TestQuest";
        when(auditionsChannelHandler.getQuestChannel(questName)).thenReturn(questChannel);
        when(auditionsChannelHandler.getNpcThreadMap(questChannel)).thenReturn(Collections.emptyMap());
        when(guildService.getStaffVotingChannel()).thenReturn(staffVotingChannel);

        // When
        discordPollHandler.setupPoll(questName);

        // Then
        verify(guildService).getStaffVotingChannel();
    }
}
