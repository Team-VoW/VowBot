package me.kmaxi.wynnvp.listeners;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Controller;

@Controller
public class TokenActivationHandler extends ListenerAdapter {


    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {

    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {


    }

    //Indicates that a user was removed from a Guild. This includes kicks, bans, and leaves respectively.
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {

    }

}