package me.kmaxi.wynnvp.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter @Setter @ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private int id;
    private String displayName;
    private String email;
    private boolean publicEmail;
    private String avatarLink;
    private String bio;
    private String lore;
    private boolean systemAdmin;
    private long discordId;
    private String discordName;
    private String youtube;
    private String twitter;
    private String castingCallClub;
    private List<RoleDTO> roles;

    @Getter @Setter @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoleDTO {
        private String name;
        private String color;
        private int weight;
    }
}