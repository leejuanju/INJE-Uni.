package com.example.scrollingshooter_3;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Projectile extends GameObject {
    private static final float PROJECTILE_WIDTH = 8;
    private static final float PROJECTILE_HEIGHT = 20;

    private int color;
    private int damage;
    private boolean visible;

    public Projectile(float x, float y, float velocityX, float velocityY, int color, int damage) {
        super(x, y, PROJECTILE_WIDTH, PROJECTILE_HEIGHT);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.color = color;
        this.damage = damage;
        this.visible = true;
    }

    @Override
    public void update() {
        x += velocityX;
        y += velocityY;

        // 화면 밖으로 나가면 안보이게 설정
        if (y < -height || y > 2000 + height || x < -width || x > 1080 + width) {
            visible = false;
        }

        updateHitBox();
    }

    @Override
    public void draw(Canvas canvas) {
        if (visible) {
            Paint paint = new Paint();
            paint.setColor(color);
            canvas.drawRect(hitBox, paint);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getDamage() {
        return damage;
    }
}
