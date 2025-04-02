package me.kmaxi.wynnvp.dtos;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class LineReportData {
    private String message;
    private String NPC;
    private String X;
    private String Y;
    private String Z;

    public LineReportData(String message, String NPC, String X, String Y, String Z) {
        this.message = message;
        this.NPC = NPC;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
    }


}
