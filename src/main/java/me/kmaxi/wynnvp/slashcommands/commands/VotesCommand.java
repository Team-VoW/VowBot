package me.kmaxi.wynnvp.slashcommands.commands;

import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.slashcommands.poll.VotersSQL;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Objects;

public class VotesCommand implements ICommandImpl {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("votes", "Gets your current votes")
                .addOptions(new OptionData(OptionType.STRING, "npc", "The NPC of which to get the poll for. If this is empty it will tell you all your votes", false));

    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ANYONE;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        String toBePrinted;

        if (event.getOption("npc") == null) {
            toBePrinted = VotersSQL.getAllVotes(event.getUser().getId());
        } else {
            toBePrinted = VotersSQL.getVotes(event.getUser().getId(), Objects.requireNonNull(event.getOption("npc")).getAsString().replace(" ", "_"));
        }
        event.getHook().editOriginal(toBePrinted).queue();

    }
}
