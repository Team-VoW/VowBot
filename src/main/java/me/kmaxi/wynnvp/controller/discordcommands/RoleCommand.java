package me.kmaxi.wynnvp.controller.discordcommands;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.interfaces.StringIntInterface;
import me.kmaxi.wynnvp.services.AuditionsHandler;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static me.kmaxi.wynnvp.BotRegister.guild;

@Component
public class RoleCommand implements ICommandImpl {


    @Autowired
    private AuditionsHandler auditionsHandler;

    private final String openSubCommand = "open";
    private final String setSubCommand = "set";

    @Override
    public CommandData getCommandData() {

        return Commands.slash("role", "Handles setting of roles of castings.")
                .addSubcommands(new SubcommandData(openSubCommand, "Sets an already set role for castings")
                        .addOptions(
                                new OptionData(OptionType.STRING, "questname", "The exact name of the quest as it is in the application channel", true),
                                new OptionData(OptionType.STRING, "npcname", "The exact name of the npc  exactly as it is in the application channel", true)))
                .addSubcommands(new SubcommandData(setSubCommand, "Sets a role that is open in the casting")
                        .addOptions(
                                new OptionData(OptionType.STRING, "questname", "The exact name of the quest as it is in the application channel", true),
                                new OptionData(OptionType.STRING, "npcname", "The exact name of the npc  exactly as it is in the application channel", true),
                                new OptionData(OptionType.USER, "user", "The person that you want to cast for this role", true)));

    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.STAFF;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String questName = Objects.requireNonNull(event.getOption("questname")).getAsString();
        String npcName = Objects.requireNonNull(event.getOption("npcname")).getAsString();

        switch (Objects.requireNonNull(event.getSubcommandName())) {
            case openSubCommand:
                event.reply(setRoleAsAvailable(questName, npcName)).setEphemeral(true).queue();
                break;
            case setSubCommand:
                event.reply(setRoleAsTaken(questName, npcName, event)).setEphemeral(true).queue();
        }
    }

    private String setRoleAsTaken(String questName, String npcName, SlashCommandInteractionEvent event) {

        User personWhoGotIt = Objects.requireNonNull(event.getOption("user")).getAsUser();
        return auditionsHandler.setRole(questName, npcName, personWhoGotIt);
    }

    private String setRoleAsAvailable(String questName, String npcName) {
        return auditionsHandler.openRole(questName, npcName);
    }
    }
