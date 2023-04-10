package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.utils.Utils;
import me.kmaxi.wynnvp.linereport.LineReportManager;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class SlashCommandsRegister extends ListenerAdapter {


    @Override
    public void onGuildReady(GuildReadyEvent event) {

        List<CommandData> commandData = new ArrayList<>();

        commandData.add(Commands.slash("help", "Get all vow bot commands"));

        //Api commands
        commandData.add(Commands.slash("update", "Manually checks if there are any lines in the index api"));
        commandData.add(Commands.slash("resetforwarded", "Moves all forwarded api entries back to index so they will be sent again."));
        commandData.add(Commands.slash("getacceptedlines", "Sends all lines marked as accepted in the current channel")
                .addOptions(
                        new OptionData(OptionType.STRING, "npcname", "The exact name of the npc", true),
                        new OptionData(OptionType.BOOLEAN, "addreaction", "If it should send the messages one at a time to allow reaction.", false)));
        commandData.add(Commands.slash("getactivelines", "Sends all lines marked as index and accepted in the current channel")
                .addOptions(
                        new OptionData(OptionType.STRING, "npcname", "The exact name of the npc", true),
                        new OptionData(OptionType.BOOLEAN, "addreaction", "If it should send the messages one at a time to allow reaction.", false)));
        commandData.add(Commands.slash("getalllines", "Sends all lines marked as index and accepted in the current channel")
                .addOptions(
                        new OptionData(OptionType.STRING, "npcname", "The exact name of the npc", true),
                        new OptionData(OptionType.BOOLEAN, "addreaction", "If it should send the messages one at a time to allow reaction.", false)));

        //Application commands
        commandData.add(Commands.slash("close", "Immediately closes an application channel")
                .addSubcommands(new SubcommandData("immediately", "Directly deletes this application channel"))
                .addSubcommands(new SubcommandData("soon", "Closes an application channel in 24h"))
        );
        commandData.add(Commands.slash("setrole", "Sets a role that is open in #vocice-apply")
                .addOptions(
                        new OptionData(OptionType.STRING, "questname", "The exact name of the quest as it is in the application channel", true),
                        new OptionData(OptionType.STRING, "npcname", "The exact name of the npc  exactly as it is in the application channel", true),
                        new OptionData(OptionType.USER, "user", "The person that you want to cast for this role", true)));

        commandData.add(Commands.slash("openrole", "Sets an already set role tp open in #vocice-apply")
                .addOptions(
                        new OptionData(OptionType.STRING, "questname", "The exact name of the quest as it is in the application channel", true),
                        new OptionData(OptionType.STRING, "npcname", "The exact name of the npc  exactly as it is in the application channel", true)));

        commandData.add(Commands.slash("addquest", "Opens casting for a new quest")
                .addOptions(
                        new OptionData(OptionType.STRING, "questname", "The name of the quest with no spaces. Use - between words", true),
                        new OptionData(OptionType.STRING, "npc1", "The firsts npc name", true),
                        new OptionData(OptionType.STRING, "npc2", "The seconds npc name", false),
                        new OptionData(OptionType.STRING, "npc3", "The thirds npc name", false),
                        new OptionData(OptionType.STRING, "npc4", "The fourths npc name", false),
                        new OptionData(OptionType.STRING, "npc5", "The fifths npc name", false),
                        new OptionData(OptionType.STRING, "npc6", "The sixths npc name", false),
                        new OptionData(OptionType.STRING, "npc7", "The sevenths npc name", false),
                        new OptionData(OptionType.STRING, "npc8", "The eights npc name", false),
                        new OptionData(OptionType.STRING, "npc9", "The ninths npc name", false)));

        commandData.add(Commands.slash("createchannel", "Creates a text channel in accepted category for a voice actor")
                .addOptions(new OptionData(OptionType.USER, "user", "The voices actors discord", true))
                .addOptions(new OptionData(OptionType.STRING, "npc", "The name of the NPC", true)));

        commandData.add(Commands.slash("syncallusers", "Syncs all users data to the website. Warning, this is a heavy command!"));
        commandData.add(Commands.slash("checked", "Upgrades this users role here and on website and creates an account if they don't have one"));

        commandData.add(Commands.slash("setuppoll", "Sets up the voting poll for the casting call")
                .addOptions(new OptionData(OptionType.STRING, "url", "The url to the casting call", true)));

        event.getGuild().updateCommands().addCommands(commandData).queue();
    }


    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {

        if (event.getMember() == null) {
            event.reply("Error. User was null").setEphemeral(true).queue();
            return;
        }

        if (!(Utils.isStaff(event.getMember()))) {
            event.reply("You do not have permission do execute this command.").setEphemeral(true).queue();
            return;
        }

        switch (event.getName().toLowerCase().trim()) {
            case "help":
                HelpCommand.TriggerCommand(event);
                return;
            case "close":
                ApplicationCommands.close(event);
                return;
            case "setrole":
                ApplicationCommands.setRoleAsTaken(event);
                return;
            case "openrole":
                ApplicationCommands.setRoleAsAvailable(event);
                return;
            case "addquest":
                ApplicationCommands.addQuest(event);
                return;
            case "update":
                LineReportManager.sendAllReports();
                return;
            case "getacceptedlines":
                ApiCommands.getAcceptedLinesFromCharacter(event);
                return;
            case "getactivelines":
                ApiCommands.getActiveLinesFromCharacter(event);
                return;
            case "getalllines":
                ApiCommands.getAllLinesFromCharacter(event);
                break;
            case "createchannel":
                ChannelCommands.CreateChannelForVoiceActor(event);
                break;
            case "checked":
                SyncWebsite.FinishedRole(event);
                break;

        }
        if (!(Utils.isAdmin(event.getMember()))) {
            event.reply("You do not have permission do execute this command.").setEphemeral(true).queue();
            return;
        }

        switch (event.getName().toLowerCase().trim()) {
            case "syncallusers":
                SyncWebsite.SyncAllUsers(event);
                break;
            case "resetforwarded":
                LineReportManager.resetForwarded();
                return;
            case "setuppoll":
                SetUpPollCommand.SetUpPoll(event);
                return;
        }
    }
}
