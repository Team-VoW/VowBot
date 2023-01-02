package me.kmaxi.wynnvp.slashcommands;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import static me.kmaxi.wynnvp.Utils.permissions;
import static me.kmaxi.wynnvp.Utils.traineePerms;

public class ChannelCommands {


    public static void CreateChannelForVoiceActor(SlashCommandInteractionEvent event){
        User user = event.getOption("user").getAsUser();
        String npcName = event.getOption("npc").getAsString();

        Guild guild = event.getGuild();
        guild.createTextChannel(getChannelName(user, npcName), guild.getCategoryById(Config.acceptedCategoryID))
                .setTopic(getTopic(user, npcName))
                .addMemberPermissionOverride(user.getIdLong(), permissions(), null)
                .addRolePermissionOverride(Config.roleID, permissions(), null)
                .addRolePermissionOverride(820690089427861535l, null, permissions())
                .addRolePermissionOverride(Config.traineeRole, traineePerms(), null)
                .addPermissionOverride(guild.getPublicRole(), null, permissions())
                .queue(textChannel -> {
                    textChannel.sendMessage("Congrats " + user.getAsMention() + " for getting the role as **" + npcName + "** :partying_face: "
                            + "\n \nPlease send in the recordings for this character in one wav file as soon as you are able to.  "
                            + "Once every person that voices a character in this quest has sent in their lines it will be added to the mod and website (https://voicesofwynn.com). "
                            + "If this is your first role then you'll get login details for your account when it's uploaded."
                            + "\n\nA staff member will send the script of this quest very soon."
                            + "\n\nBy voicing this character, you agreed to the terms listed in " + guild.getTextChannelById(820027818799792129L).getAsMention()).queue();
                });

        event.reply("Sucesfully created the \"" + getChannelName(user, npcName) + "\" channel").setEphemeral(true).queue();

    }

    private static String getChannelName(User user, String npcName){
        return npcName + "-" + user.getName();
    }

    private static String getTopic (User user, String npcName) {
        return user.getName() + "s channel for the role of " + npcName;
    }
}
