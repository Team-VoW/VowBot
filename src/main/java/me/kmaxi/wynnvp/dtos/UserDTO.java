package me.kmaxi.wynnvp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class UserDTO {
    private int userId;
    private String displayName;
    private long discordId;
    private String discordName;
    private String avatarUrl;
    private PictureType pictureType;
    private List<String> roleNames;

    public enum PictureType {
        @JsonProperty("Default")
        DEFAULT,
        @JsonProperty("Discord")
        DISCORD,
        @JsonProperty("Manual")
        MANUAL
    }
}
