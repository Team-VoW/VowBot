package me.kmaxi.wynnvp.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class LineReportDTO {
    @JsonProperty("message")
    private String message;

    @JsonProperty("NPC")
    private String npc;

    @JsonProperty("X")
    private String x;

    @JsonProperty("Y")
    private String y;

    @JsonProperty("Z")
    private String z;

    @JsonProperty("reporter")
    private String reporter; // Can be null

    // Default constructor required for Jackson JSON deserialization
    public LineReportDTO() {}
    public LineReportDTO(String message, String npc, String x, String y, String z) {
        this.message = message;
        this.npc = npc;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
