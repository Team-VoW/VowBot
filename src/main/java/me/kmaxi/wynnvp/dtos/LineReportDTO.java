package me.kmaxi.wynnvp.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineReportDTO {
    private String chatMessage;
    private String npcName;
    private PositionDTO position;

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PositionDTO {
        private int x;
        private int y;
        private int z;
    }
}
