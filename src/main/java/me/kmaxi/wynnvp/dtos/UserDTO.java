package me.kmaxi.wynnvp.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.kmaxi.wynnvp.utils.MemberUtils;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

@Getter
@Setter
@ToString
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

    @Getter
    @Setter
    @ToString
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoleDTO {
        private String name;
        private String color;
        private int weight;

        public RoleDTO() {
            // Default constructor needed for Jackson
        }

        public RoleDTO(String roleName) {
            this.name = roleName;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            RoleDTO roleDTO = (RoleDTO) obj;
            return name != null && name.equals(roleDTO.name);
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }

    public String getFullPostArguments() {
        return "discordName=" + discordName + "&" +
                "discordId=" + discordId + "&" +
                "roles=" + getRolesArguments() + "&" +
                "imgurl=" + avatarLink;
    }

    public String getChangingArguments() {
        return "discordName=" + discordName + "&" +
                "discordId=" + discordId + "&" +
                "roles=" + getRolesArguments();
    }

    private String getRolesArguments() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");

        //For each role user has in discord
        roles.forEach(role -> {
            String roleName = role.getName();

            stringBuilder.append("\"").append(roleName).append("\",");
        });
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}