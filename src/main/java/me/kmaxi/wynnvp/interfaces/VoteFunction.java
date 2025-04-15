package me.kmaxi.wynnvp.interfaces;


public interface VoteFunction {
    boolean apply(String pollName, String messageId, String userId);
}
