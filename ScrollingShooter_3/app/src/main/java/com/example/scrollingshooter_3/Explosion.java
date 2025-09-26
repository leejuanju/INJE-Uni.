package com.example.scrollingshooter_3;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Explosion extends GameObject {
    private static final int EXPLOSION_DURATION = 10;
    private static final float EXPLOSION_MAX_RADIUS = 40;

    private int frame;
    private boolean isBossExplosion;
    private Paint paint;

    public Explosion(float x, float y) {
        this(x, y, false);
    }

    public Explosion(float x, float y, boolean isBossExplosion) {
        super(x, y, 0, 0);
        this.frame = 0;
        this.isBossExplosion = isBossExplosion;

        paint = new Paint();
        paint.setColor(Color.YELLOW);
    }

    @Override
    public void update() {
        frame++;

        // 폭발 효과 크기 계산
        float progress = (float) frame / EXPLOSION_DURATION;
        float radius = isBossExplosion ?
                EXPLOSION_MAX_RADIUS * 3 * (1 - Math.abs(progress - 0.5f) * 2) :
                EXPLOSION_MAX_RADIUS * (1 - Math.abs(progress - 0.5f) * 2);

        this.width = radius * 2;
        this.height = radius * 2;

        updateHitBox();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!isFinished()) {
            // 폭발 색상 변경
            if (frame < EXPLOSION_DURATION / 3) {
                paint.setColor(Color.WHITE);
            } else if (frame < EXPLOSION_DURATION * 2 / 3) {
                paint.setColor(Color.YELLOW);
            } else {
                paint.setColor(Color.RED);
            }

            canvas.drawCircle(x, y, width / 2, paint);
        }
    }

    public boolean isFinished() {
        return frame >= EXPLOSION_DURATION;
    }
}
