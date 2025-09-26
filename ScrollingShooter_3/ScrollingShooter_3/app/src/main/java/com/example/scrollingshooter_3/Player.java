package com.example.scrollingshooter_3;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

public class Player extends GameObject {
    private static final float PLAYER_WIDTH = 100;
    private static final float PLAYER_HEIGHT = 80;
    private static final int MAX_HEALTH = 100;
    private static final long FIRE_INTERVAL = 300; // 미사일 발사 간격(ms)

    private int health;
    private long lastFireTime;
    private Paint paint;
    private int screenWidth;
    private int screenHeight;

    private Bitmap bitmap;

    public Player() {
        super(0, 0, PLAYER_WIDTH, PLAYER_HEIGHT);

    }

    public void setInfo(Context context, float x, float y, int screenWidth, int screenHeight){

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.plane);
        // 이미지 크기 조정
        bitmap = Bitmap.createScaledBitmap(bitmap, (int)width, (int)height, false);
        this.health = MAX_HEALTH;
        this.lastFireTime = System.currentTimeMillis();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        paint = new Paint();
        paint.setColor(Color.WHITE);
    }
    @Override
    public void update() {
        // 위치 업데이트 (상하좌우 모두 적용)
        x += velocityX;
        y += velocityY;  // y 속도도 적용

        // 화면 경계 체크 (x축)
        if (x < width/2) {
            x = width/2;
        } else if (x > screenWidth - width/2) {
            x = screenWidth - width/2;
        }

        // 화면 경계 체크 (y축) - 상하 이동 범위 제한
        float minY = height/2 + screenHeight * 0.5f;  // 화면 중간 아래로만
        float maxY = screenHeight - height/2;
        if (y < minY) {
            y = minY;
        } else if (y > maxY) {
            y = maxY;
        }

        // 히트박스 업데이트
        updateHitBox();
    }
    @Override
    public void draw(Canvas canvas) {

        // 사각형 대신 비행기 이미지 그리기
        canvas.drawBitmap(bitmap, x - width/2, y - height/2, null);
        // 플레이어 비행기 그리기
        //canvas.drawRect(hitBox, paint);

        // 체력바 그리기
        Paint healthPaint = new Paint();
        healthPaint.setColor(Color.GREEN);
        float healthWidth = (width * health) / MAX_HEALTH;
        canvas.drawRect(x - width/2, y + height/2 + 10,
                x - width/2 + healthWidth, y + height/2 + 20, healthPaint);
    }

    public void fireIfReady(List<Projectile> projectiles) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFireTime > FIRE_INTERVAL) {
            lastFireTime = currentTime;



            // 기본 미사일
            projectiles.add(new Projectile(x, y - height/2, 0, -15, Color.YELLOW, 10));
            // 좌우 미사일도 추가 (이미지의 3개 미사일 발사 패턴과 유사하게)
            projectiles.add(new Projectile(x - 30, y - height/3, 0, -15, Color.YELLOW, 10));
            projectiles.add(new Projectile(x + 30, y - height/3, 0, -15, Color.YELLOW, 10));
        }
    }

    // Player.java에 추가해야 할 getter 메서드들
    public float getWidth() {
        return width;
    }

    public int getScreenWidth() {
        return screenWidth;
    }
    // Player.java에 추가
    public void setX(float newX) {
        this.x = newX;
        updateHitBox();  // 위치 변경 후 히트박스 업데이트
    }
    // Player.java에 추가
    public void setY(float newY) {
        this.y = newY;
        updateHitBox();  // 위치 변경 후 히트박스 업데이트
    }

    public float getHeight() {
        return height;
    }

    public int getScreenHeight() {
        return screenHeight;
    }


    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public int getHealth() {
        return health;
    }


    // Player.java (멀티플레이어용 정보 클래스)


}
// Player.java (멀티플레이어용 정보 클래스)
class MultiPlayer extends Player{
    public String playerId;
    public String name;
    public boolean isHost;
    public int score;
    public float posX;
    public float posY;


    public MultiPlayer(String playerId, String name, boolean isHost) {
        super();
        this.playerId = playerId;
        this.name = name;
        this.isHost = isHost;
        this.score = 0;
    }
}