package me.kmaxi.wynnvp.listeners;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.interfaces.VoteFunction;
import me.kmaxi.wynnvp.slashcommands.poll.PollSQL;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class ButtonClickedListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Button button = event.getButton();

        if (button.getLabel().equals(Config.voteButtonLabel) || button.getLabel().equals(Config.removeVoteButtonLabel)) {
            processVote(event);
        }
    }


    private void processVote(ButtonInteractionEvent event) {
        //The id is in this format: "[-]entryId" (if the - is there, the button is "Unvote". Otherwise it's "Vote"
        System.out.println(event.getUser().getName() + " clicked: " + event.getButton().getId() + " with label: " + event.getButton().getLabel());
        int buttonId = event.getButton().getId().toInt(); //TODO idk how to parse string to int
        int entryId = Math.abs(buttonId); //TODO idk how do math functions work in Java – just need to get rid of the minus sign
        String vaName = splitID[2];

        if (entryId == buttonId) {
            //Button ID was already positive --> adding vote
            VoteFunction = PollSQL::addVote;
            String action = "Vote";
        } else {
            //Button ID was negative --> removing vote
            VoteFunction = PollSQL::removeVote;
            String action = "Unvote";
        }

        try {
            if (voteFunction.apply(entryId, event.getUser().getId()))
                event.reply(action + "d " + vaName + " for the role of " + roleName + ".").setEphemeral(true).queue();
            else
                event.reply("Could **not*** " + action.toLowerCase() + " " + vaName + "for the role of " + roleName + "!").setEphemeral(true).queue(); //TODO idk how to convert action to lowercase
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

