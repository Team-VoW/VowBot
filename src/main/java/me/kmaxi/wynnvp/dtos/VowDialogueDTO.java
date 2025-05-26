package me.kmaxi.wynnvp.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @JsonIgnoreProperties(ignoreUnknown = true)
public class VowDialogueDTO {
    private String line;
    private String file;
    private boolean onPlayer = false;
    private int fallOff = 0;
    private String npc;
    private boolean stopSounds = true;
}