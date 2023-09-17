package me.kmaxi.wynnvp;

public enum ServerRespons {

    CORRECT,
    WRONG,
    SERVER_DOWN,
    SERVER_ERROR;


    public static ServerRespons mapResponse(int response) {
        return switch (response) {
            case 200 -> CORRECT;
            case 201 -> WRONG;
            case 500 -> SERVER_DOWN;
            default -> SERVER_ERROR;
        };
    }
}
