package me.kmaxi.wynnvp.enums;

import lombok.Getter;

/** What to do with a batch of lines. DELETE removes the reports outright rather than restatusing them. */
@Getter
public enum SetLinesCommand {
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    VOICED("fixed"),
    DELETE(null);

    private final String apiStatus;

    SetLinesCommand(String apiStatus) {
        this.apiStatus = apiStatus;
    }

    public boolean isDelete() {
        return apiStatus == null;
    }
}
