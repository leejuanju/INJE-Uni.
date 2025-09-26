package com.example.scrollingshooter_3;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.FirebaseApp;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private GameView gameView;
    private FirebaseDatabase database;
    private DatabaseReference roomsRef;
    private FirebaseAuth auth;

    private GameRoom gameRoom;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        // 전체화면 설정
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Firebase 초기화
        database = FirebaseDatabase.getInstance();
        roomsRef = database.getReference("rooms");
        auth = FirebaseAuth.getInstance();

        // 익명 로그인 시도
        boolean[] isAuthenticated = {false}; // 배열을 사용하여 익명 클래스 내에서 수정 가능하게 함

        auth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 로그인 성공
                        isAuthenticated[0] = true;
                        Toast.makeText(this, "Firebase 인증 성공", Toast.LENGTH_SHORT).show();
                    } else {
                        // 로그인 실패
                        Toast.makeText(this, "Firebase 인증 실패: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });


        // 게임 메뉴 레이아웃 설정
        setContentView(R.layout.activity_main);

        // 버튼 초기화 - 반드시 레이아웃에 이 버튼들을 추가해야 함
         Button singlePlayerButton = findViewById(R.id.singlePlayerButton);
        Button multiPlayerButton = findViewById(R.id.multiPlayerButton);

        singlePlayerButton.setOnClickListener(v -> startSinglePlayerGame());
        multiPlayerButton.setOnClickListener(v -> showMultiplayerOptions());
    }

    private void startSinglePlayerGame() {
        // 기존 게임뷰로 설정
        setContentView(R.layout.game_layout);
        gameView = new GameView(this);
        FrameLayout gameContainer = findViewById(R.id.game_container);
        gameContainer.addView(gameView);
    }

    private void showMultiplayerOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("멀티플레이어");
        String[] options = {"방 만들기", "방 참여하기"};

        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                showCreateRoomDialog();
            } else {
                startActivity(new Intent(MainActivity.this, RoomListActivity.class));
            }
        });

        builder.show();
    }

    private void showCreateRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("방 만들기");

        // 플레이어 이름 입력 필드
        final EditText input = new EditText(this);
        input.setHint("플레이어 이름");
        builder.setView(input);

        builder.setPositiveButton("만들기", (dialog, which) -> {
            String playerName = input.getText().toString();
            if (!playerName.isEmpty()) {
                createRoom(playerName);
            } else {
                Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void createRoom(String playerName) {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "인증에 실패했습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        // 고유한 방 ID 생성
        String roomId = roomsRef.push().getKey();
        String playerId = auth.getCurrentUser().getUid();

        // 새 게임방 객체 생성
        GameRoom newRoom = new GameRoom(roomId, playerId, playerName);

        // Firebase에 방 정보 저장
        roomsRef.child(roomId).setValue(newRoom)
                .addOnSuccessListener(aVoid -> {
                    // 방 생성 성공, 멀티플레이어 게임 화면으로 이동
                    Intent intent = new Intent(MainActivity.this, MultiplayerGameActivity.class);
                    intent.putExtra("ROOM_ID", roomId);
                    intent.putExtra("PLAYER_ID", playerId);
                    intent.putExtra("IS_HOST", true);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "방 생성 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        // 연결 종료 시 방 데이터 삭제
        DatabaseReference playerRef = roomsRef.child(roomId).child("players").child(playerId);
        playerRef.onDisconnect().removeValue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pause();
        }
    }
}
