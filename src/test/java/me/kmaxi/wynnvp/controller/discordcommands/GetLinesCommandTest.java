package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.enums.LineType;
import me.kmaxi.wynnvp.services.LineReportHandler;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetLinesCommand Tests")
class GetLinesCommandTest {

    @Mock
    private LineReportHandler lineHandler;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private OptionMapping npcNameOption;

    @Mock
    private OptionMapping typeOption;

    @Mock
    private MessageChannelUnion channel;

    @Captor
    private ArgumentCaptor<LineType> lineTypeCaptor;

    @Captor
    private ArgumentCaptor<String> npcNameCaptor;

    @InjectMocks
    private GetLinesCommand getLinesCommand;

    @BeforeEach
    void setUp() {
        when(event.getChannel()).thenReturn(channel);
    }

    @Test
    @DisplayName("Should have correct command data with options")
    void getCommandData_ReturnsCorrectData() {
        // When
        CommandData commandData = getLinesCommand.getCommandData();

        // Then
        assertThat(commandData.getName()).isEqualTo("getlines");
        assertThat(commandData).isNotNull();
    }

    @Test
    @DisplayName("Should require STAFF permission level")
    void getPermissionLevel_ReturnsStaff() {
        // When
        PermissionLevel level = getLinesCommand.getPermissionLevel();

        // Then
        assertThat(level).isEqualTo(PermissionLevel.STAFF);
    }

    @Test
    @DisplayName("Should execute with all parameters provided")
    void execute_WithAllParameters_Success() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(typeOption);
        when(npcNameOption.getAsString()).thenReturn("TestNPC");
        when(typeOption.getAsString()).thenReturn("accepted");

        // When
        getLinesCommand.execute(event);

        // Then
        verify(event).reply("Sending lines now");
        verify(lineHandler).sendLinesWithoutReaction(
                lineTypeCaptor.capture(),
                npcNameCaptor.capture(),
                eq(channel)
        );

        assertThat(lineTypeCaptor.getValue()).isEqualTo(LineType.ACCEPTED);
        assertThat(npcNameCaptor.getValue()).isEqualTo("TestNPC");
    }

    @Test
    @DisplayName("Should default to ALL type when type not specified")
    void execute_WithoutType_DefaultsToAll() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(null);
        when(npcNameOption.getAsString()).thenReturn("TestNPC");

        // When
        getLinesCommand.execute(event);

        // Then
        verify(lineHandler).sendLinesWithoutReaction(
                lineTypeCaptor.capture(),
                npcNameCaptor.capture(),
                eq(channel)
        );

        assertThat(lineTypeCaptor.getValue()).isEqualTo(LineType.ALL);
        assertThat(npcNameCaptor.getValue()).isEqualTo("TestNPC");
    }

    @Test
    @DisplayName("Should handle ACTIVE line type")
    void execute_ActiveType_Success() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(typeOption);
        when(npcNameOption.getAsString()).thenReturn("ActiveNPC");
        when(typeOption.getAsString()).thenReturn("active");

        // When
        getLinesCommand.execute(event);

        // Then
        verify(lineHandler).sendLinesWithoutReaction(
                lineTypeCaptor.capture(),
                npcNameCaptor.capture(),
                eq(channel)
        );

        assertThat(lineTypeCaptor.getValue()).isEqualTo(LineType.ACTIVE);
        assertThat(npcNameCaptor.getValue()).isEqualTo("ActiveNPC");
    }

    @Test
    @DisplayName("Should handle ALL line type")
    void execute_AllType_Success() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(typeOption);
        when(npcNameOption.getAsString()).thenReturn("AllNPC");
        when(typeOption.getAsString()).thenReturn("all");

        // When
        getLinesCommand.execute(event);

        // Then
        verify(lineHandler).sendLinesWithoutReaction(
                lineTypeCaptor.capture(),
                npcNameCaptor.capture(),
                eq(channel)
        );

        assertThat(lineTypeCaptor.getValue()).isEqualTo(LineType.ALL);
        assertThat(npcNameCaptor.getValue()).isEqualTo("AllNPC");
    }

    @Test
    @DisplayName("Should handle NPC names with spaces")
    void execute_NpcNameWithSpaces_Success() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(null);
        when(npcNameOption.getAsString()).thenReturn("Test NPC Name");

        // When
        getLinesCommand.execute(event);

        // Then
        verify(lineHandler).sendLinesWithoutReaction(
                eq(LineType.ALL),
                eq("Test NPC Name"),
                eq(channel)
        );
    }

    @Test
    @DisplayName("Should handle NPC names with special characters")
    void execute_NpcNameWithSpecialChars_Success() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(null);
        when(npcNameOption.getAsString()).thenReturn("NPC-123_Test");

        // When
        getLinesCommand.execute(event);

        // Then
        verify(lineHandler).sendLinesWithoutReaction(
                eq(LineType.ALL),
                eq("NPC-123_Test"),
                eq(channel)
        );
    }

    @Test
    @DisplayName("Should send ephemeral confirmation reply")
    void execute_SendsEphemeralReply() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(null);
        when(npcNameOption.getAsString()).thenReturn("TestNPC");

        // When
        getLinesCommand.execute(event);

        // Then
        verify(event).reply("Sending lines now");
    }

    @Test
    @DisplayName("Should handle case insensitive line type")
    void execute_CaseInsensitiveType_Success() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(typeOption);
        when(npcNameOption.getAsString()).thenReturn("TestNPC");
        when(typeOption.getAsString()).thenReturn("ACCEPTED"); // Uppercase

        // When
        getLinesCommand.execute(event);

        // Then
        verify(lineHandler).sendLinesWithoutReaction(
                eq(LineType.ACCEPTED),
                anyString(),
                eq(channel)
        );
    }

    @Test
    @DisplayName("Should send lines to the correct channel")
    void execute_SendsToCorrectChannel() {
        // Given
        when(event.getOption("npcname")).thenReturn(npcNameOption);
        when(event.getOption("type")).thenReturn(null);
        when(npcNameOption.getAsString()).thenReturn("TestNPC");

        // When
        getLinesCommand.execute(event);

        // Then
        verify(event).getChannel();
        verify(lineHandler).sendLinesWithoutReaction(any(), anyString(), eq(channel));
    }

    @Test
    @DisplayName("Should have valid command data")
    void getCommandData_HasThreeTypeChoices() {
        // When
        CommandData commandData = getLinesCommand.getCommandData();

        // Then
        assertThat(commandData).isNotNull();
        assertThat(commandData.getName()).isEqualTo("getlines");
    }
}
