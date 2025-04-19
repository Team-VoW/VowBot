package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.RequiredArgsConstructor;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.audition.AuditionsHandler;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.springframework.stereotype.Component;

import java.util.Objects;

@RequiredArgsConstructor
@Component
public class RoleCommand implements ICommandImpl {
    private final AuditionsHandler auditionsHandler;
    private static final String OPEN_SUB_COMMAND = "open";
    private static final String SET_SUB_COMMAND = "set";

    @Override
    public CommandData getCommandData() {

        OptionData questNameOption = new OptionData(OptionType.STRING, "questname", "The exact name of the quest as it is in the application channel", true);
        OptionData npcNameOption = new OptionData(OptionType.STRING, "npcname", "The exact name of the npc exactly as it is in the application channel", true);

        return Commands.slash("role", "Handles setting of roles of castings.")
                .addSubcommands(new SubcommandData(OPEN_SUB_COMMAND, "Sets an already set role for castings")
                        .addOptions(
                                questNameOption,
                                npcNameOption))
                .addSubcommands(new SubcommandData(SET_SUB_COMMAND, "Sets a role that is open in the casting")
                        .addOptions(
                                questNameOption,
                                npcNameOption,
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
            case OPEN_SUB_COMMAND:
                event.reply(setRoleAsAvailable(questName, npcName)).setEphemeral(true).queue();
                break;
            case SET_SUB_COMMAND:
                event.reply(setRoleAsTaken(questName, npcName, event)).setEphemeral(true).queue();
                break;
            default:
                event.reply("Unknown subcommand").setEphemeral(true).queue();
                break;
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
