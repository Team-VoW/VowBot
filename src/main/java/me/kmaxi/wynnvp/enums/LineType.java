package me.kmaxi.wynnvp.enums;


import lombok.Getter;

@Getter
public enum LineType {
    ACCEPTED("accepted"),
    ACTIVE("active"),
    ALL("valid");

    private final String apiKeyword;

    LineType(String apiKeyword) {
        this.apiKeyword = apiKeyword;
    }
}