package me.kmaxi.wynnvp.handlers;

import lombok.RequiredArgsConstructor;
import me.kmaxi.wynnvp.interfaces.ICommandImpl;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SlashCommandHandler {

    Map<String, ICommandImpl> commands;

}
