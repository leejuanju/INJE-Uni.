package com.example.scrollingshooter_3;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;
import java.util.Random;

public class Enemy extends GameObject {
    private static final float ENEMY_WIDTH = 80;
    private static final float ENEMY_HEIGHT = 60;

    private int health;
    private int maxHealth;
    private Paint paint;
    private Random random;
    private int screenWidth;

    private Context context; // 리소스 로드를 위한 컨텍스트
    private float angle;     // 비행기 회전 각도
    private Bitmap enemyBitmap; // 비행기 이미지

    public Enemy(Context context,float x, float y, int screenWidth, int screenHeight) {
        super(x, y, ENEMY_WIDTH, ENEMY_HEIGHT);

        this.context = context;
        this.random = new Random();
        this.maxHealth = 20;
        this.health = maxHealth;
        this.screenWidth = screenWidth;
        this.angle = 0; // 기본 각도

        // 적의 속도 설정
        this.velocityY = 3 + random.nextFloat() * 2;
        this.velocityX = random.nextFloat() * 4 - 2; // -2 ~ 2 사이의 속도

        // 이미지 로드 및 크기 조정 (context가 null이 아닌 경우에만)
        if (context != null) {
            enemyBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.plane2);
            enemyBitmap = Bitmap.createScaledBitmap(enemyBitmap, (int)width, (int)height, false);
        }

        paint = new Paint();
        paint.setColor(Color.rgb(0, 150, 0));//녹색 적비행기
    }

    @Override
    public void update() {
        // 위치 업데이트
        x += velocityX;
        y += velocityY;

        // 좌우 경계에 닿으면 방향 전환
        if (x < width/2 || x > screenWidth - width/2) {
            velocityX = -velocityX;
        }

        // 히트박스 업데이트
        updateHitBox();
    }

    @Override
    public void draw(Canvas canvas) {
        // 적 비행기 그리기
       // canvas.drawRect(hitBox, paint);
        if (enemyBitmap != null) {
            canvas.save();
            // 비행기 회전 (x,y는 중심점이므로 그대로 사용)
            canvas.rotate(angle, x, y);
            // 이미지를 중심에 맞춰 그리기
            canvas.drawBitmap(enemyBitmap, x - width/2, y - height/2, null);
            canvas.restore();
        } else {
            // 이미지가 없을 경우 사각형으로 그리기
            canvas.drawRect(hitBox, paint);
        }

        // 체력바 그리기
        Paint healthPaint = new Paint();
        healthPaint.setColor(Color.RED);
        float healthWidth = (width * health) / maxHealth;
        canvas.drawRect(x - width/2, y - height/2 - 10,
                x - width/2 + healthWidth, y - height/2 - 5, healthPaint);
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public void fire(List<Projectile> projectiles,int level) {
        float missileSpeed = 7 * (1 + level * 0.15f);
        projectiles.add(new Projectile(x, y + height/2, 0, missileSpeed, Color.GREEN, 10));
    }
}
