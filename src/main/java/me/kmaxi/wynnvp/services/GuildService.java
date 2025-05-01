package me.kmaxi.wynnvp.services;

import lombok.Getter;
import lombok.Setter;
import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class GuildService {

    private Guild guild;

    public TextChannel getStaffVotingChannel() {
        return guild.getTextChannelById(Config.STAFF_VOTING_CHANNEL_ID);
    }
}