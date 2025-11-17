package me.kmaxi.wynnvp.services.audition;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.GuildService;
import me.kmaxi.wynnvp.services.MemberHandler;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditionsHandler Tests")
class AuditionsHandlerTest {

    @Mock
    private MemberHandler memberHandler;

    @Mock
    private GuildService guildService;

    @Mock
    private AuditionsChannelHandler auditionsChannelHandler;

    @Mock
    private MessageChannel channel;

    @Mock
    private Message message;

    @Mock
    private Member member;

    @Mock
    private Guild guild;

    @Mock
    private User user;

    @Mock
    private NewsChannel newsChannel;

    @Mock
    private MessageHistory messageHistory;

    @Mock
    private MessageCreateAction messageCreateAction;

    @Mock
    private RestAction<Message> messageRestAction;

    @Mock
    private RestAction<Void> reactionRestAction;

    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @InjectMocks
    private AuditionsHandler auditionsHandler;

    @BeforeEach
    void setUp() {
        // Setup common mocks
        when(channel.sendMessage(anyString())).thenReturn(messageCreateAction);
        when(messageCreateAction.queue(any())).thenAnswer(invocation -> {
            java.util.function.Consumer<Message> consumer = invocation.getArgument(0);
            consumer.accept(message);
            return null;
        });
        when(message.addReaction(any(Emoji.class))).thenReturn(reactionRestAction);
        when(reactionRestAction.queue()).thenReturn(null);
    }

    @Test
    @DisplayName("Should create poll with correct format for multiple NPCs")
    void setupPoll_MultipleNpcs_CreatesCorrectPoll() {
        // Given
        String questName = "The Lost City";
        List<String> npcs = Arrays.asList("Guard John", "Merchant Mary", "Wizard Bob");

        // When
        auditionsHandler.setupPoll(questName, npcs, channel);

        // Then
        verify(channel).sendMessage(messageCaptor.capture());
        String sentMessage = messageCaptor.getValue();

        assertThat(sentMessage).contains("React to apply for a role in The Lost City");
        assertThat(sentMessage).contains("Guard John");
        assertThat(sentMessage).contains("Merchant Mary");
        assertThat(sentMessage).contains("Wizard Bob");
        assertThat(sentMessage).contains("regional_indicator_a");
        assertThat(sentMessage).contains("regional_indicator_b");
        assertThat(sentMessage).contains("regional_indicator_c");
    }

    @Test
    @DisplayName("Should limit poll to 26 NPCs maximum")
    void setupPoll_MoreThan26Npcs_LimitsTo26() {
        // Given
        String questName = "Epic Quest";
        List<String> npcs = Arrays.asList(
                "NPC1", "NPC2", "NPC3", "NPC4", "NPC5", "NPC6", "NPC7", "NPC8", "NPC9", "NPC10",
                "NPC11", "NPC12", "NPC13", "NPC14", "NPC15", "NPC16", "NPC17", "NPC18", "NPC19", "NPC20",
                "NPC21", "NPC22", "NPC23", "NPC24", "NPC25", "NPC26", "NPC27", "NPC28"
        );

        // When
        auditionsHandler.setupPoll(questName, npcs, channel);

        // Then
        verify(channel).sendMessage(messageCaptor.capture());
        String sentMessage = messageCaptor.getValue();

        // Should include NPC1-NPC26 but not NPC27, NPC28
        assertThat(sentMessage).contains("NPC26");
        assertThat(sentMessage).doesNotContain("NPC27");
        assertThat(sentMessage).doesNotContain("NPC28");
    }

    @Test
    @DisplayName("Should add reactions for each NPC in the poll")
    void setupPoll_AddsReactionsForEachNpc() {
        // Given
        String questName = "Test Quest";
        List<String> npcs = Arrays.asList("NPC1", "NPC2", "NPC3");

        // When
        auditionsHandler.setupPoll(questName, npcs, channel);

        // Then - Should add 3 reactions
        verify(message, times(3)).addReaction(any(Emoji.class));
    }

    @Test
    @DisplayName("Should create quest channels after poll setup")
    void setupPoll_CreatesQuestChannels() {
        // Given
        String questName = "Test Quest";
        List<String> npcs = Arrays.asList("NPC1", "NPC2");

        // When
        auditionsHandler.setupPoll(questName, npcs, channel);

        // Then
        verify(auditionsChannelHandler).createQuestChannels(questName, npcs);
    }

    @Test
    @DisplayName("Should handle finished role for new actor with account creation")
    void finishedRole_NewActor_CreatesAccount() {
        // Given
        String expectedPassword = "TempPass123";
        when(member.getAsMention()).thenReturn("<@123456>");
        when(member.getId()).thenReturn("123456");
        when(member.getEffectiveName()).thenReturn("TestUser");
        when(member.getUser()).thenReturn(user);
        when(user.getName()).thenReturn("TestUser");

        CompletableFuture<Void> upgradeFuture = new CompletableFuture<>();
        when(memberHandler.upgradeActorRole(member, guild)).thenReturn(upgradeFuture);
        when(memberHandler.createAccount(member)).thenReturn(expectedPassword);

        RestAction<Member> memberRestAction = mock(RestAction.class);
        when(guild.retrieveMemberById(anyString())).thenReturn(memberRestAction);
        when(memberRestAction.queueAfter(anyLong(), any(), any())).thenAnswer(invocation -> {
            java.util.function.Consumer<Member> consumer = invocation.getArgument(2);
            consumer.accept(member);
            return null;
        });

        upgradeFuture.complete(null);

        // When
        CompletableFuture<String> result = auditionsHandler.finishedRole(member, guild);

        // Then - Wait for async completion
        assertThat(result).isNotNull();
        verify(memberHandler).upgradeActorRole(member, guild);
    }

    @Test
    @DisplayName("Should handle finished role for expert actor without upgrade")
    void finishedRole_ExpertActor_NoUpgrade() {
        // Given
        when(member.getAsMention()).thenReturn("<@123456>");
        when(member.getEffectiveName()).thenReturn("ExpertUser");
        when(memberHandler.upgradeActorRole(member, guild)).thenReturn(null);

        // When
        CompletableFuture<String> result = auditionsHandler.finishedRole(member, guild);

        // Then
        assertThat(result).isCompletedWithValueMatching(msg ->
                msg.contains("expert actor") && msg.contains("role stayed the same"));
    }

    @Test
    @DisplayName("Should return error when quest not found")
    void setRole_QuestNotFound_ReturnsError() {
        // Given
        when(guildService.getGuild()).thenReturn(guild);
        when(guild.getNewsChannelById(Config.VOICE_APPLY_CHANNEL_ID)).thenReturn(newsChannel);
        when(newsChannel.getHistoryFromBeginning(100)).thenReturn(messageHistory);
        when(messageHistory.complete()).thenReturn(messageHistory);
        when(messageHistory.getRetrievedHistory()).thenReturn(Arrays.asList());

        // When
        String result = auditionsHandler.setRole("NonexistentQuest", "SomeNPC", user);

        // Then
        assertThat(result).contains("Could not find quest");
    }

    @Test
    @DisplayName("Should return error when NPC not found")
    void setRole_NpcNotFound_ReturnsError() {
        // Given
        when(guildService.getGuild()).thenReturn(guild);
        when(guild.getNewsChannelById(Config.VOICE_APPLY_CHANNEL_ID)).thenReturn(newsChannel);
        when(newsChannel.getHistoryFromBeginning(100)).thenReturn(messageHistory);
        when(messageHistory.complete()).thenReturn(messageHistory);
        when(messageHistory.getRetrievedHistory()).thenReturn(Arrays.asList());

        // When
        String result = auditionsHandler.setRole("ValidQuest", "NonexistentNPC", user);

        // Then
        assertThat(result).contains("Could not find");
    }

    @Test
    @DisplayName("Should return error when quest not found for openRole")
    void openRole_QuestNotFound_ReturnsError() {
        // Given
        when(guildService.getGuild()).thenReturn(guild);
        when(guild.getNewsChannelById(Config.VOICE_APPLY_CHANNEL_ID)).thenReturn(newsChannel);
        when(newsChannel.getHistoryFromBeginning(100)).thenReturn(messageHistory);
        when(messageHistory.complete()).thenReturn(messageHistory);
        when(messageHistory.getRetrievedHistory()).thenReturn(Arrays.asList());

        // When
        String result = auditionsHandler.openRole("NonexistentQuest", "SomeNPC");

        // Then
        assertThat(result).contains("Could not find quest");
    }

    @Test
    @DisplayName("Should handle single NPC poll")
    void setupPoll_SingleNpc_CreatesCorrectPoll() {
        // Given
        String questName = "Simple Quest";
        List<String> npcs = Arrays.asList("Solo NPC");

        // When
        auditionsHandler.setupPoll(questName, npcs, channel);

        // Then
        verify(channel).sendMessage(messageCaptor.capture());
        String sentMessage = messageCaptor.getValue();

        assertThat(sentMessage).contains("Simple Quest");
        assertThat(sentMessage).contains("Solo NPC");
        assertThat(sentMessage).contains("regional_indicator_a");
        verify(message, times(1)).addReaction(any(Emoji.class));
    }

    @Test
    @DisplayName("Should format poll message with proper markdown")
    void setupPoll_ProperMarkdownFormatting() {
        // Given
        String questName = "Test Quest";
        List<String> npcs = Arrays.asList("Test NPC");

        // When
        auditionsHandler.setupPoll(questName, npcs, channel);

        // Then
        verify(channel).sendMessage(messageCaptor.capture());
        String sentMessage = messageCaptor.getValue();

        assertThat(sentMessage).startsWith(">>>");
        assertThat(sentMessage).contains("**React to apply for a role in Test Quest**");
    }
}
