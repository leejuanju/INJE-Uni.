package com.example.scrollingshooter_3;


import android.graphics.Canvas;
import android.graphics.RectF;

public abstract class GameObject {
    protected float x;
    protected float y;
    protected float width;
    protected float height;
    protected float velocityX;
    protected float velocityY;
    protected RectF hitBox;

    public GameObject(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.velocityX = 0;
        this.velocityY = 0;
        this.hitBox = new RectF(x - width/2, y - height/2, x + width/2, y + height/2);
    }

    public abstract void update();
    public abstract void draw(Canvas canvas);

    public void updateHitBox() {
        hitBox.left = x - width/2;
        hitBox.top = y - height/2;
        hitBox.right = x + width/2;
        hitBox.bottom = y + height/2;
    }

    public RectF getHitBox() {
        return hitBox;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }
}