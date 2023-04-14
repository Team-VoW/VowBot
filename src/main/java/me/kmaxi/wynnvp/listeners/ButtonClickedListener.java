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

        if (button.getLabel().equals(Config.voteButtonLabel)) {
            processVote(event, PollSQL::addVote, "vote");
        } else if (button.getLabel().equals(Config.removeVoteButtonLabel)) {
            processVote(event, PollSQL::removeVote, "unvote");
        }
    }


    private void processVote(ButtonInteractionEvent event, VoteFunction voteFunction, String action) {
        //The id is in this format: "RoleName-AuditionNumber-VaName-Label"
        //Label is just there so that Vote and Remove vote have different ids
        String[] splitID = event.getId().split("-");
        String roleName = splitID[0];
        String vaName = splitID[2];

        try {
            if (voteFunction.apply(roleName, vaName, event.getUser().getId()))
                event.reply(action + "ed for " + roleName + "-" + vaName).setEphemeral(true).queue();
            else
                event.reply("Could NOT " + action + " for " + roleName + "-" + vaName).setEphemeral(true).queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Execute the passed function with calculated parameters
        try {
            voteFunction.apply(roleName, vaName, event.getUser().getId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
