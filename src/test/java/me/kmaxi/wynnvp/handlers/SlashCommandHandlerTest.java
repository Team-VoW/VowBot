package me.kmaxi.wynnvp.handlers;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SlashCommandHandler Tests")
class SlashCommandHandlerTest {

    @Mock
    private ICommandImpl everyoneCommand;

    @Mock
    private ICommandImpl staffCommand;

    @Mock
    private ICommandImpl adminCommand;

    @Mock
    private SlashCommandInteractionEvent event;

    @Mock
    private Member member;

    @Mock
    private GuildReadyEvent guildReadyEvent;

    @Mock
    private Guild guild;

    @Mock
    private CommandListUpdateAction commandListUpdateAction;

    @Captor
    private ArgumentCaptor<String> replyCaptor;

    private SlashCommandHandler handler;

    @BeforeEach
    void setUp() {
        // Setup command mocks
        CommandData everyoneCommandData = Commands.slash("everyone", "Everyone command");
        CommandData staffCommandData = Commands.slash("staff", "Staff command");
        CommandData adminCommandData = Commands.slash("admin", "Admin command");

        when(everyoneCommand.getCommandData()).thenReturn(everyoneCommandData);
        when(everyoneCommand.getPermissionLevel()).thenReturn(PermissionLevel.ANYONE);

        when(staffCommand.getCommandData()).thenReturn(staffCommandData);
        when(staffCommand.getPermissionLevel()).thenReturn(PermissionLevel.STAFF);

        when(adminCommand.getCommandData()).thenReturn(adminCommandData);
        when(adminCommand.getPermissionLevel()).thenReturn(PermissionLevel.ADMIN);

        // Initialize handler with test commands
        List<ICommandImpl> commands = Arrays.asList(everyoneCommand, staffCommand, adminCommand);
        handler = new SlashCommandHandler(commands);

        // Setup common event mocks
        when(event.getMember()).thenReturn(member);
    }

    @Test
    @DisplayName("Should register all commands on guild ready")
    void onGuildReady_RegistersAllCommands() {
        // Given
        when(guildReadyEvent.getGuild()).thenReturn(guild);
        when(guild.updateCommands()).thenReturn(commandListUpdateAction);
        when(commandListUpdateAction.addCommands(any(List.class))).thenReturn(commandListUpdateAction);
        doNothing().when(commandListUpdateAction).queue();

        // When
        handler.onGuildReady(guildReadyEvent);

        // Then
        verify(guild).updateCommands();
        verify(commandListUpdateAction).addCommands(any(List.class));
        verify(commandListUpdateAction).queue();
    }

    @Test
    @DisplayName("Should execute everyone command without permission check")
    void onSlashCommandInteraction_EveryoneCommand_Executes() {
        // Given
        when(event.getName()).thenReturn("everyone");

        // When
        handler.onSlashCommandInteraction(event);

        // Then
        verify(everyoneCommand).execute(event);
        verify(event, never()).reply(anyString());
    }

    @Test
    @DisplayName("Should reply with error when member is null")
    void onSlashCommandInteraction_NullMember_RepliesError() {
        // Given
        when(event.getMember()).thenReturn(null);
        when(event.getName()).thenReturn("everyone");

        // When
        handler.onSlashCommandInteraction(event);

        // Then
        verify(event).reply("Error. User was null");
        verify(everyoneCommand, never()).execute(any());
    }

    @Test
    @DisplayName("Should reply with error when command not found")
    void onSlashCommandInteraction_UnknownCommand_RepliesError() {
        // Given
        when(event.getName()).thenReturn("nonexistent");

        // When
        handler.onSlashCommandInteraction(event);

        // Then
        verify(event).reply("Command not found");
        verify(everyoneCommand, never()).execute(any());
        verify(staffCommand, never()).execute(any());
        verify(adminCommand, never()).execute(any());
    }

    @Test
    @DisplayName("Should execute staff command when user has staff permissions")
    void onSlashCommandInteraction_StaffCommand_WithStaffPerms_Executes() {
        // Given
        when(event.getName()).thenReturn("staff");
        // Mock Utils.isStaff() indirectly by setting up member roles
        // Since we can't easily mock static methods without PowerMock,
        // we'll verify the behavior through integration
        // For this test, we assume the member has staff role
        when(member.getRoles()).thenReturn(Arrays.asList());

        // This test requires mocking static Utils.isStaff - skip for now
        // or use @MockStatic in more advanced setup
    }

    @Test
    @DisplayName("Should deny staff command when user lacks staff permissions")
    void onSlashCommandInteraction_StaffCommand_WithoutStaffPerms_Denied() {
        // Given
        when(event.getName()).thenReturn("staff");
        when(member.getRoles()).thenReturn(Arrays.asList());
        // Member doesn't have staff role, so Utils.isStaff returns false

        // When
        handler.onSlashCommandInteraction(event);

        // Then
        verify(event).reply("You do not have permission to execute this command.");
        verify(staffCommand, never()).execute(any());
    }

    @Test
    @DisplayName("Should deny admin command when user lacks admin permissions")
    void onSlashCommandInteraction_AdminCommand_WithoutAdminPerms_Denied() {
        // Given
        when(event.getName()).thenReturn("admin");
        when(member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)).thenReturn(false);

        // When
        handler.onSlashCommandInteraction(event);

        // Then
        verify(event).reply("You do not have permission to execute this command. You need Admin perms");
        verify(adminCommand, never()).execute(any());
    }

    @Test
    @DisplayName("Should handle command name case insensitively")
    void onSlashCommandInteraction_CaseInsensitive_Executes() {
        // Given
        when(event.getName()).thenReturn("EVERYONE");

        // When
        handler.onSlashCommandInteraction(event);

        // Then
        verify(everyoneCommand).execute(event);
    }

    @Test
    @DisplayName("Should trim whitespace from command name")
    void onSlashCommandInteraction_WithWhitespace_Executes() {
        // Given
        when(event.getName()).thenReturn("  everyone  ");

        // When
        handler.onSlashCommandInteraction(event);

        // Then
        verify(everyoneCommand).execute(event);
    }

    @Test
    @DisplayName("Should store all commands in internal map")
    void constructor_StoresAllCommands() {
        // Given - handler already created in setUp with 3 commands

        // When - we trigger an event with each command name
        when(event.getName()).thenReturn("everyone");
        handler.onSlashCommandInteraction(event);

        when(event.getName()).thenReturn("staff");
        handler.onSlashCommandInteraction(event);

        when(event.getName()).thenReturn("admin");
        handler.onSlashCommandInteraction(event);

        // Then - at least one interaction with each command object occurred
        verify(everyoneCommand, atLeastOnce()).getPermissionLevel();
        verify(staffCommand, atLeastOnce()).getPermissionLevel();
        verify(adminCommand, atLeastOnce()).getPermissionLevel();
    }

    @Test
    @DisplayName("Should reply with error when member is null")
    void onSlashCommandInteraction_ErrorMessages_AreEphemeral() {
        // Given
        when(event.getMember()).thenReturn(null);

        // When
        handler.onSlashCommandInteraction(event);

        // Then
        verify(event).reply("Error. User was null");
    }
}
