package me.kmaxi.wynnvp.listeners;

import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.utils.APIUtils;
import me.kmaxi.wynnvp.APIKeys;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.Objects;

public class TokenActivationHandler extends ListenerAdapter {

    private static final String deactivateURL = "https://voicesofwynn.com/api/premium/deactivate";
    private static final String reactivateURL = "https://voicesofwynn.com/api/premium/reactivate";

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        //Do not care about the removal of this role as this role had nothing to do with the vow cloud access
        if (!Config.vowCloudAccessRoles.contains(event.getRoles()))
            return;

        reactiveToken(event.getMember());
    }

    @Override
    public void onGuildMemberRoleRemove(@Nonnull GuildMemberRoleRemoveEvent event) {
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
    public void onGuildMemberRemove(@Nonnull GuildMemberRemoveEvent event) {
        if (event.getMember() == null) {
            Objects.requireNonNull(event.getGuild().getTextChannelById(Config.staffBotChat)).sendMessage("ERROR! LEAVING MEMBER WAS NULL!").queue();
            return;
        }

        disableToken(event.getMember());
    }


    private void disableToken(Member member) {
        sendRequest(deactivateURL, member);
    }

    private void reactiveToken(Member member) {
        sendRequest(reactivateURL, member);
    }

    private void sendRequest(String url, Member member) {

        HttpURLConnection conn = APIUtils.sendPostRequest(url, "&discord=" + member.getId() + "&apiKey=" + APIKeys.vowCloudAPIKey);

        StringBuilder result = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Sent request to: " + url + " with member " + member.getEffectiveName() + " and got result: " + result);
    }


}
