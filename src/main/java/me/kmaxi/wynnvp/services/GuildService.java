package me.kmaxi.wynnvp.services;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class GuildService {

    private Guild guild;
}