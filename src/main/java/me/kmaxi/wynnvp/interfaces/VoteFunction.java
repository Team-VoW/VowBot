package me.kmaxi.wynnvp.interfaces;

import java.sql.SQLException;

public interface VoteFunction {
    boolean apply(String pollName, String messageId, String userId)  throws SQLException;
}
