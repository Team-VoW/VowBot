package me.kmaxi.wynnvp.services;


import me.kmaxi.wynnvp.Config;
import me.kmaxi.wynnvp.services.data.AccountService;
import me.kmaxi.wynnvp.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AuditionsHandler {

    @Autowired
    private AccountService accountService;

    public void setupPoll(String questName, List<String> npcs, MessageChannel channel){
        ArrayList<String> reactions = new ArrayList<>();
        String out = ">>> **React to apply for a role in " + questName + "**";
        int i = 1;
        for (String npc : npcs) {
            if (i == 10) {
                break;
            }
            out += "\n:" + Utils.convertNumber(i) + ": = " + npc + "\n";
            reactions.add(String.valueOf(i));
            i++;
        }

        channel.sendMessage(out).queue(message1 -> {
            int index = 1;
            for (String reaction : reactions) {
                message1.addReaction(Emoji.fromUnicode(Utils.getUnicode(index))).queue();
                index++;
            }
        });
    }

    public CompletableFuture<String> finishedRole(Member member, Guild guild) {
        CompletableFuture<Void> completableFuture = upgradeActorRole(member, guild);

        // Role was not upgraded because person is already at highest role
        if (completableFuture == null) {
            return CompletableFuture.completedFuture("Thanks a lot for voicing this character " + member.getAsMention() + ":heart:. " +
                    "\nBecause you already are expert actor your role stayed the same this time :grin:");
        }

        return completableFuture.thenComposeAsync(v -> {
            CompletableFuture<String> resultFuture = new CompletableFuture<>();
            guild.retrieveMemberById(member.getId()).queueAfter(1, TimeUnit.SECONDS, updatedMember -> {
                try {
                    String password = accountService.createAccount(member);

                    if (password.isEmpty()) {
                        resultFuture.complete("Thanks a lot for voicing this character " + member.getAsMention() + ":heart:. " +
                                "\nYour actor role has been upgraded here and on the Website :partying_face:");
                    } else {
                        resultFuture.complete("Thanks a lot for voicing your very first character for us " + member.getAsMention() + ":heart::partying_face:." +
                                "\n\n An account with the name " + member.getUser().getName() + " and the temporary password ||" +
                                password + "|| has been created for you on our website https://voicesofwynn.com/ " +
                                "\n\n Once everyone voice actor from this quest has sent in their lines, everything will be " +
                                " added to all the voice actors accounts. Feel free to go in there and change your bio, profile picture and more! :grin:");
                    }
                } catch (IOException e) {
                }
            });
            return resultFuture;
        });
    }

    private CompletableFuture<Void> upgradeActorRole(Member member, Guild guild) {
        List<Role> roleList = member.getRoles();
        for (Role role : roleList) {
            if (!Config.actorRoleList.contains(role.getId()))
                continue;

            int indexOfRole = Config.actorRoleList.indexOf(role.getId());

            //If the user is at the highest role already
            if (indexOfRole == Config.actorRoleList.size() - 1) {
                return null;
            }
            guild.removeRoleFromMember(member, role).queue();

            System.out.println("Upgraded role of " + member.getEffectiveName() + " to actor tier " + indexOfRole + 1);

            return guild.addRoleToMember(member, Objects.requireNonNull(guild.getRoleById(Config.actorRoleList.get(indexOfRole + 1)))).submit();
        }

        System.out.println("Added first actor role to " + member.getEffectiveName());
        return guild.addRoleToMember(member, Objects.requireNonNull(guild.getRoleById(Config.actorRoleList.get(0)))).submit();
    }
}