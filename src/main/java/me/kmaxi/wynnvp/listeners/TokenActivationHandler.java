package me.kmaxi.wynnvp.listeners;

import me.kmaxi.wynnvp.APIKeys;
import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.utils.APIUtils;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.Objects;

public class TokenActivationHandler extends ListenerAdapter {

    private static final String deactivateURL = "https://voicesofwynn.com/api/premium/deactivate";
    private static final String reactivateURL = "https://voicesofwynn.com/api/premium/reactivate";

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        //Do not care about the removal of this role as this role had nothing to do with the vow cloud access
        if (!Config.hasVowCloudAccess(event.getRoles()))
            return;

        reactiveToken(event.getMember());
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        //Do not care about the removal of this role as this role had nothing to do with the vow cloud access
        if (!Config.hasVowCloudAccess(event.getRoles()))
            return;

        //The user still has vow cloud access even though they lost a role that gave them access
        if (Config.hasVowCloudAccess(event.getMember().getRoles()))
            return;
        disableToken(event.getMember());

    }

    //Indicates that a user was removed from a Guild. This includes kicks, bans, and leaves respectively.
    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (event.getMember() == null) {
            Objects.requireNonNull(event.getGuild().getTextChannelById(Config.staffBotChat)).sendMessage("ERROR! LEAVING MEMBER WAS NULL!").queue();
            return;
        }

        disableToken(event.getMember());
    }


    private void disableToken(Member member) {
        System.out.println("Disabling token for " + member.getEffectiveName());
        sendRequest(deactivateURL, member);
    }

    private void reactiveToken(Member member) {
        System.out.println("Enabling token for " + member.getEffectiveName());

        sendRequest(reactivateURL, member);
    }

    private void sendRequest(String url, Member member) {

        int resposeCode = 0;
        try {
            resposeCode = APIUtils.sendPUT(url, "discord=" + member.getId(), APIKeys.vowCloudAPIKey);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Sent request to: " + url + " with member " + member.getEffectiveName() + " and got result: " + resposeCode);
    }


}
