package me.kmaxi.wynnvp.controller.discordcommands;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.kmaxi.wynnvp.PermissionLevel;
import me.kmaxi.wynnvp.dtos.UserDTO;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import me.kmaxi.wynnvp.services.GuildService;
import me.kmaxi.wynnvp.services.data.UserService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class SynAllUsersCommand implements ICommandImpl {

    private final UserService userService;

    private final GuildService guildService;

    @Override
    public CommandData getCommandData() {
        return Commands.slash("syncallusers", "Syncs all users data to the website. Warning, this is a heavy command!");
    }

    @Override
    public PermissionLevel getPermissionLevel() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        List<UserDTO> users = userService.getAllUsers();
        event.deferReply().queue();

        //For each website account
        for (UserDTO user : users) {
            syncUser(user);
        }
        event.getHook().setEphemeral(true).editOriginal("Synced all users").queue();
    }


    /**
     * Syncs website users if they are in the discord
     *
     * @param userInfo The Json object of the website user
     */
    private void syncUser(UserDTO userInfo) {

        String discordUserName = "";

        //For some reason a regular null check does not work, so we just check the string value
        if (userInfo.getDiscordName() != null) {
            discordUserName = userInfo.getDiscordName();
        }

        long uuidOnWebsite = userInfo.getDiscordId();

        Member member = getDiscordMember(uuidOnWebsite, discordUserName);

        if (member == null) {
            log.info("User {} with discord: {} is not in the discord", userInfo.getDisplayName(), discordUserName);
            return;
        }


        try {
            userService.setUserIfNeeded(member, userInfo);
        } catch (IOException e) {
            log.error("Failed to sync user " + userInfo.getDisplayName() + " with discord: " + discordUserName, e);
        }
    }


    /**
     * Gets the discord member from either UUID, if a player with that exists, if no player with
     * the uuid exists then it tries getting the member via the username
     *
     * @param uuid            The uuid of the member
     * @param discordUserName The discord name of the member, only used if no person with the UUID was found
     * @return returns the discord member
     */
    private Member getDiscordMember(long uuid, String discordUserName) {
        Guild guild = guildService.getGuild();

        if (uuid != 0) {
            return guild.getMemberById(uuid);
        }

        if (discordUserName.isEmpty() || discordUserName.contains("#"))
            return null;


        return guild.getMembersByName(discordUserName, false).stream().findFirst().orElse(null);
    }


}
