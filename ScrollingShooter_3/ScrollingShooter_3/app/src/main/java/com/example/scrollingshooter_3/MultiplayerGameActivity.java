package com.example.scrollingshooter_3;


import android.os.Bundle;
import android.widget.FrameLayout;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiplayerGameActivity extends AppCompatActivity {
    private MultiplayerGameView gameView;
    private String roomId;
    private String playerId;
    private boolean isHost;
    private DatabaseReference roomRef;
    private DatabaseReference playerRef;
    private ScheduledExecutorService scheduledExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 전체화면 설정
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_multiplayer_game);

        // Firebase 정보 가져오기
        roomId = getIntent().getStringExtra("ROOM_ID");
        playerId = getIntent().getStringExtra("PLAYER_ID");
        isHost = getIntent().getBooleanExtra("IS_HOST", false);

        // Firebase 참조 생성
        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
        playerRef = roomRef.child("players").child(playerId);

        // 게임 시작 상태 업데이트
        if (isHost) {
            roomRef.child("isGameStarted").setValue(true);
        }

        // 게임 뷰 초기화
        gameView = new MultiplayerGameView(this, roomId, playerId, isHost);
        FrameLayout gameContainer = findViewById(R.id.gameContainer);
        gameContainer.addView(gameView);

        // 위치 업데이트 타이머 설정
        setupPositionUpdates();

        // 상대방 플레이어 모니터링
        listenForOpponentUpdates();
    }

    private void setupPositionUpdates() {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (gameView != null && gameView.getPlayer() != null) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("posX", gameView.getPlayer().getX());
                updates.put("posY", gameView.getPlayer().getY());
                playerRef.updateChildren(updates);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void listenForOpponentUpdates() {
        roomRef.child("players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerKey = playerSnapshot.getKey();
                    if (playerKey != null && !playerKey.equals(playerId)) {
                        Float posX = playerSnapshot.child("posX").getValue(Float.class);
                        Float posY = playerSnapshot.child("posY").getValue(Float.class);

                        if (posX != null && posY != null) {
                            runOnUiThread(() -> {
                                gameView.updateOpponentPosition(posX, posY);
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // 오류 처리
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 스케줄러 종료
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }

        // 방 정리
        if (isHost) {
            roomRef.removeValue();
        } else {
            playerRef.removeValue();
        }
    }
}