package me.kmaxi.wynnvp.listeners;

import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.interfaces.VoteFunction;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@Slf4j
public class ButtonClickedListener extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Button button = event.getButton();

        if (button.getLabel().equals(Config.voteButtonLabel)) {
            log.info("Vote button clicked by {} with id: {}", event.getUser().getName(), event.getButton().getId());
            //processVote(event, PollSQL::addVote, "vote");

        } else if (button.getLabel().equals(Config.removeVoteButtonLabel)) {
            //processVote(event, PollSQL::removeVote, "unvote");
            log.info("Remove vote button clicked by {} with id: {}", event.getUser().getName(), event.getButton().getId());
        }
    }


    private void processVote(ButtonInteractionEvent event, VoteFunction voteFunction, String action) {
        //The id is in this format: "RoleName-AuditionNumber-VaName-Label"
        //Label is just there so that Vote and Remove vote have different ids
        System.out.println(event.getUser().getName() + " clicked: " + event.getButton().getId() + " with label: " + event.getButton().getLabel());
        String[] splitID = Objects.requireNonNull(event.getButton().getId()).split("-");
        String roleName = splitID[0];
        String vaName = splitID[2];


        if (voteFunction.apply(roleName.replaceAll(".`", ""), vaName, event.getUser().getId()))
            event.reply(action + "d for " + vaName + " as role " + roleName).setEphemeral(true).queue();
        else
            event.reply("Could NOT " + action + " for " + roleName + "-" + vaName).setEphemeral(true).queue();

        // Execute the passed function with calculated parameters

        voteFunction.apply(roleName, vaName, event.getUser().getId());

    }


}
