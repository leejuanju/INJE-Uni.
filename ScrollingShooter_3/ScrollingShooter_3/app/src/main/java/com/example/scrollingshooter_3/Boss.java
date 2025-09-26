package com.example.scrollingshooter_3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;
import java.util.Random;

public class Boss extends GameObject {
    private static final float BOSS_WIDTH = 300;
    private static final float BOSS_HEIGHT = 200;


    private int health;
    private int maxHealth;
    private int currentPhase;
    private int phaseCounter;
    private long lastFireTime;
    private Paint paint;
    private Random random;
    private int screenWidth;

    private Bitmap bossBitmap;
    private int level;

    public Boss(Context context, float x, float y, int screenWidth, int level) {
        super(x, y, BOSS_WIDTH, BOSS_HEIGHT);

        this.level = level;
        this.maxHealth = 500 + level * 100;
        this.health = maxHealth;
        this.currentPhase = 0;
        this.phaseCounter = 0;
        this.lastFireTime = System.currentTimeMillis();
        this.screenWidth = screenWidth;


        int imageResource;
        switch (level % 4) { // 3개의 보스 이미지를 순환
            case 0:
                imageResource = R.drawable.plane6;
                break;
            case 1:
                imageResource = R.drawable.plane3;
                break;
            case 2:
                imageResource = R.drawable.plane5;
                break;
            case 3:
                imageResource = R.drawable.plane5;
                break;
            default:
                imageResource = R.drawable.plane3;
        }

        // 이미지 로드 및 크기 조정
        bossBitmap = BitmapFactory.decodeResource(context.getResources(), imageResource);
        bossBitmap = Bitmap.createScaledBitmap(bossBitmap, (int)width, (int)height, false);

        paint = new Paint();
        paint.setColor(Color.rgb(200, 180, 50)); // 금색 보스 (이미지와 유사)
        random = new Random();
    }

    @Override
    public void update() {
        phaseCounter++;

        // 체력에 따른 패턴 변경
        float healthPercent = (float) health / maxHealth;

        if (healthPercent > 0.7f) {
            // 페이즈 1: 좌우 이동
            executePhase1();
        } else if (healthPercent > 0.3f) {
            // 페이즈 2: 8자 패턴
            executePhase2();
        } else {
            // 페이즈 3: 불규칙 이동
            executePhase3();
        }

        // 경계 체크
        if (x < width/2) {
            x = width/2;
            velocityX = Math.abs(velocityX);
        } else if (x > screenWidth - width/2) {
            x = screenWidth - width/2;
            velocityX = -Math.abs(velocityX);
        }

        updateHitBox();
    }

    private void executePhase1() {
        // 좌우 이동 패턴
        velocityX = (float) Math.sin(phaseCounter * 0.05) * 5;
        velocityY = 0;

        x += velocityX;
        y += velocityY;
    }

    private void executePhase2() {
        // 8자 패턴
        velocityX = (float) Math.sin(phaseCounter * 0.05) * 8;
        velocityY = (float) Math.sin(phaseCounter * 0.1) * 3;

        x += velocityX;
        y += velocityY;
    }

    private void executePhase3() {
        // 불규칙 이동
        if (phaseCounter % 60 == 0) {
            velocityX = random.nextFloat() * 10 - 5;
            velocityY = random.nextFloat() * 4 - 2;
        }

        x += velocityX;
        y += velocityY;
    }

    @Override
    public void draw(Canvas canvas) {
        // 보스 그리기
        //canvas.drawRect(hitBox, paint);

        // 이미지로 보스 그리기
        if (bossBitmap != null) {
            canvas.drawBitmap(bossBitmap, x - width/2, y - height/2, null);
        } else {
            // 이미지가 없을 경우 기본 사각형 대신 그리기
            canvas.drawRect(hitBox, paint);
        }

        // 체력바 그리기
        Paint healthPaint = new Paint();
        healthPaint.setColor(Color.GREEN);
        float healthWidth = (width * health) / maxHealth;
        canvas.drawRect(x - width/2, y - height/2 - 20,
                x - width/2 + healthWidth, y - height/2 - 10, healthPaint);
    }

    public void fireProjectiles(List<Projectile> projectiles,int level) {
        long currentTime = System.currentTimeMillis();
        float healthPercent = (float) health / maxHealth;

        // 체력에 따라 발사 간격 조절
        long fireInterval = healthPercent > 0.5f ? 1000 : 500;

        // 레벨에 따라 미사일 속도 증가 (기본 속도에 레벨당 15% 증가)
        float missileSpeed = 8 * (1 + level * 0.15f);

        if (currentTime - lastFireTime > fireInterval) {
            lastFireTime = currentTime;

            if (healthPercent > 0.7f) {
                // 페이즈 1: 직선 발사
                projectiles.add(new Projectile(x - 50, y + height/2, 0, missileSpeed, Color.GREEN, 20));
                projectiles.add(new Projectile(x + 50, y + height/2, 0, missileSpeed, Color.GREEN, 20));
            } else if (healthPercent > 0.3f) {
                // 페이즈 2: 대각선 발사
                projectiles.add(new Projectile(x - 70, y + height/2, -3, missileSpeed, Color.GREEN, 15));
                projectiles.add(new Projectile(x, y + height/2, 0, missileSpeed, Color.GREEN, 15));
                projectiles.add(new Projectile(x + 70, y + height/2, missileSpeed, 7, Color.GREEN, 15));
            } else {
                // 페이즈 3: 원형 발사 (이미지의 패턴과 유사)
                int bulletCount = 8;
                for (int i = 0; i < bulletCount; i++) {
                    double angle = 2 * Math.PI * i / bulletCount;//너무어려우면 수정
                    float vx = (float) Math.cos(angle) * (missileSpeed*0.6f);
                    float vy = (float) Math.sin(angle) * (missileSpeed*0.6f);
                    if (vy < 0) vy = Math.abs(vy); // 아래쪽으로만 발사
                    projectiles.add(new Projectile(x, y, vx, vy, Color.GREEN, 10));
                }
            }
        }
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public int getCurrentPhase() {
        float healthPercent = (float) health / maxHealth;
        if (healthPercent > 0.7f) return 1;
        else if (healthPercent > 0.3f) return 2;
        else return 3;
    }
}