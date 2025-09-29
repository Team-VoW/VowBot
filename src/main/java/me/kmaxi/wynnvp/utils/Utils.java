package me.kmaxi.wynnvp.utils;

import me.kmaxi.wynnvp.Config;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {
    private Utils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static boolean isStaff(Member member) {
        for (Role role : member.getRoles()) {
            if (role.getIdLong() == Config.VOICE_MANGER_ID
                    || role.getIdLong() == Config.WRITE_ROLE_ID) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdmin(Member member) {
        return member.hasPermission(Permission.ADMINISTRATOR);
    }
    
    public static String getChannelName(String input) {
        return input.replaceAll("[^a-zA-Z0-9-]", "");
    }

    public static String convertNumber(int number) {
        return switch (number) {
            case 1 -> "one";
            case 2 -> "two";
            case 3 -> "three";
            case 4 -> "four";
            case 5 -> "five";
            case 6 -> "six";
            case 7 -> "seven";
            case 8 -> "eight";
            case 9 -> "nine";
            default -> ":x:";
        };
    }
    
    public static String convertLetter(int alphabeticOrder) {
        return (alphabeticalOrder < 27 && alphabeticalOrder > 0) ? "regional_indicator_" + Character.toString ((char) 64 + alphabeticalOrder) : ":x:";
    }

    public static String getUnicode(int number) {
        return switch (number) {
            case 1 -> "1️⃣";
            case 2 -> "2️⃣";
            case 3 -> "3️⃣";
            case 4 -> "4️⃣";
            case 5 -> "5️⃣";
            case 6 -> "6️⃣";
            case 7 -> "7️⃣";
            case 8 -> "8️⃣";
            case 9 -> "9️⃣";
            default -> "❌";
        };
    }
    
    public static String getUnicode(char letter) {
        return switch (letter) {
            case 'A' -> "🇦"
            case 'B' -> "🇧"
            case 'C' -> "🇨"
            case 'D' -> "🇩"
            case 'E' -> "🇪"
            case 'F' -> "🇫"
            case 'G' -> "🇬"
            case 'H' -> "🇭"
            case 'I' -> "🇮"
            case 'J' -> "🇯"
            case 'K' -> "🇰"
            case 'L' -> "🇱"
            case 'M' -> "🇲"
            case 'N' -> "🇳"
            case 'O' -> "🇴"
            case 'P' -> "🇵"
            case 'Q' -> "🇶"
            case 'R' -> "🇷"
            case 'S' -> "🇸"
            case 'T' -> "🇹"
            case 'U' -> "🇺"
            case 'V' -> "🇻"
            case 'W' -> "🇼"
            case 'X' -> "🇽"
            case 'Y' -> "🇾"
            case 'Z' -> "🇿"
            default -> "❌"
        }
    }

    public static int whichNumberWasReacted(String emote) {
        return switch (emote) {
            case "1️⃣" -> 1;
            case "2️⃣" -> 2;
            case "3️⃣" -> 3;
            case "4️⃣" -> 4;
            case "5️⃣" -> 5;
            case "6️⃣" -> 6;
            case "7️⃣" -> 7;
            case "8️⃣" -> 8;
            case "9️⃣" -> 9;
            default -> 0;
        };
    }
    
    public static char whichLetterWasReacted(String emote) {
        return switch (emote) {
            "🇦" -> 'A';
            "🇧" -> 'B';
            "🇨" -> 'C';
            "🇩" -> 'D';
            "🇪" -> 'E';
            "🇫" -> 'F';
            "🇬" -> 'G';
            "🇭" -> 'H';
            "🇮" -> 'I';
            "🇯" -> 'J';
            "🇰" -> 'K';
            "🇱" -> 'L';
            "🇲" -> 'M';
            "🇳" -> 'N';
            "🇴" -> 'O';
            "🇵" -> 'P';
            "🇶" -> 'Q';
            "🇷" -> 'R';
            "🇸" -> 'S';
            "🇹" -> 'T';
            "🇺" -> 'U';
            "🇻" -> 'V';
            "🇼" -> 'W';
            "🇽" -> 'X';
            "🇾" -> 'Y';
            "🇿" -> 'Z';
            default -> 0;
        };
    }


    public static void sendPrivateMessage(User user, String content) {
        // openPrivateChannel provides a RestAction<PrivateChannel>
        // which means it supplies you with the resulting channel
        user.openPrivateChannel().queue(channel ->
                channel.sendMessage(content).queue());
    }


    public static Collection<Permission> permissions() {
        Collection<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.MESSAGE_SEND);
        permissions.add(Permission.MESSAGE_HISTORY);
        permissions.add(Permission.MESSAGE_EMBED_LINKS);
        permissions.add(Permission.MESSAGE_ATTACH_FILES);
        permissions.add(Permission.MESSAGE_ADD_REACTION);
        permissions.add(Permission.MESSAGE_EXT_EMOJI);
        permissions.add(Permission.VIEW_CHANNEL);
        return permissions;
    }

    public static Collection<Permission> traineePerms() {
        Collection<Permission> permissions = new ArrayList<>();
        permissions.add(Permission.MESSAGE_HISTORY);
        permissions.add(Permission.MESSAGE_ADD_REACTION);
        return permissions;
    }

    public static List<Message> getMessageHistory(MessageChannel textChannel, int limit) {
        int stepSize = 100;

        List<Message> messageList = textChannel.getHistory().retrievePast(stepSize).complete();
        limit -= stepSize;

        int loopsRan = 1;
        while (limit > 0){

            //If we have reached the end of the history, break. This is calculated using module
            //And by making sure that loopsRan * stepSize is not equal to messageList.size()
            if (loopsRan * stepSize != messageList.size()){
                return messageList;
            }

            if (limit < stepSize){
                stepSize = limit;
            }

            limit -= stepSize;
            messageList.addAll(textChannel.getHistoryBefore(messageList.get(messageList.size() - 1).getIdLong(), stepSize).complete().getRetrievedHistory());
            loopsRan++;
        }
        return messageList;
    }
}
