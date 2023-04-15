package me.kmaxi.wynnvp.interfaces;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SendFunction {
    void send(SlashCommandInteractionEvent event, String message);
}
