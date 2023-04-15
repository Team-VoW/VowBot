package me.kmaxi.wynnvp.slashcommands.poll;

public class VoteData {
    public String characterName;
    public int votes;
    public String voterIDS;

    public VoteData(String characterName, int votes, String voterIDS) {
        this.characterName = characterName;
        this.votes = votes;
        this.voterIDS = voterIDS;
    }
}
