package me.kmaxi.wynnvp.enums;

import lombok.Getter;

@Getter
public enum SetLinesCommand {
    ACCEPTED("y"),
    REJECTED("n"),
    VOICED("v"),
    DELETE("r");
    private final String shorthand;

    SetLinesCommand(String shorthand) {
        this.shorthand = shorthand;
    }
}
