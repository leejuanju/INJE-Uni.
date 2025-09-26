package com.example.scrollingshooter_3;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

// GameView를 상속받아 멀티플레이어 기능 확장
public class MultiplayerGameView extends GameView {
    private Player opponentPlayer;
    private String roomId;
    private String playerId;
    private boolean isHost;
    private DatabaseReference roomRef;
    private Paint opponentPaint;

    public MultiplayerGameView(Context context, String roomId, String playerId, boolean isHost) {
        super(context);
        this.roomId = roomId;
        this.playerId = playerId;
        this.isHost = isHost;

        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);

        // 상대방 플레이어 초기 설정
        opponentPaint = new Paint();
        opponentPaint.setColor(Color.YELLOW);
    }

    // 상대방 위치 업데이트
    public void updateOpponentPosition(float x, float y) {
        if (opponentPlayer == null) {
            // 기본 Player 클래스를 사용하지만 다른 색상 적용
            opponentPlayer = new Player();
            opponentPlayer.setInfo(getContext(), x, y, getWidth(), getHeight());
        } else {
            // 위치만 업데이트
            opponentPlayer.setX(x);
            opponentPlayer.setY(y);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // 상대방 플레이어 그리기
        if (canvas != null && opponentPlayer != null) {
            opponentPlayer.draw(canvas);

            // 텍스트로 상대방 표시
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(30);
            canvas.drawText("상대방", opponentPlayer.getX(),
                    opponentPlayer.getY() - opponentPlayer.getHeight(), textPaint);
        }
    }

    @Override
    public void update() {
        super.update();

        // 추가 멀티플레이어 로직이 필요한 경우 여기에 구현
    }

    // Player getter 추가
    public Player getPlayer() {
        return opponentPlayer;
    }
}