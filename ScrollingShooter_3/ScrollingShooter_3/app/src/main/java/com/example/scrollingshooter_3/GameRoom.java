package com.example.scrollingshooter_3;

import static java.security.AccessController.getContext;

import java.util.HashMap;
import java.util.Map;


// GameRoom.java
public class GameRoom {
    public String roomId;
    public Map<String, MultiPlayer> players;
    public boolean isGameStarted;
    public long createdAt;

    public GameRoom() {
        // Firebase에서 데이터 매핑을 위한 빈 생성자 필요
    }

    public GameRoom(String roomId, String hostPlayerId, String hostPlayerName) {
        this.roomId = roomId;
        this.players = new HashMap<>();
        this.players.put(hostPlayerId, new MultiPlayer(hostPlayerId, hostPlayerName, true));
        this.isGameStarted = false;
        this.createdAt = System.currentTimeMillis();
    }
}