package me.kmaxi.wynnvp.enums;

import java.util.List;
import lombok.Getter;

/**
 * Which report statuses a listing covers. The API used to expose one endpoint per set
 * (/accepted, /active, /valid); it now takes the statuses as a query parameter.
 */
@Getter
public enum LineType {
    ACCEPTED(List.of("accepted")),
    ACTIVE(List.of("accepted", "forwarded", "unprocessed")),
    ALL(List.of("fixed", "accepted", "forwarded", "unprocessed"));

    private final List<String> statuses;

    LineType(List<String> statuses) {
        this.statuses = statuses;
    }
}
