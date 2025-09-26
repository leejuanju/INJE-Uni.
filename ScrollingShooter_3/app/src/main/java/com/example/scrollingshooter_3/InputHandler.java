package com.example.scrollingshooter_3;


import android.view.KeyEvent;
import android.view.MotionEvent;

public class InputHandler {
    private float touchX;
    private float touchY;
    private boolean isTouching;

    // InputHandler.java의 handleTouchInput 메서드 수정
    public boolean handleTouchInput(MotionEvent event, Player player) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                touchY = event.getY();
                isTouching = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isTouching) {
                    float newTouchX = event.getX();
                    float newTouchY = event.getY();
                    float dx = newTouchX - touchX;
                    float dy = newTouchY - touchY;

                    // 터치로 플레이어를 상하좌우로 이동
                    player.setVelocityX(dx * 1.0f);
                    player.setVelocityY(dy * 1.0f);

                    touchX = newTouchX;
                    touchY = newTouchY;
                }
                break;

            case MotionEvent.ACTION_UP:
                isTouching = false;
                player.setVelocityX(0);
                player.setVelocityY(0);
                break;
        }
        return true;
    }

    public boolean handleKeyInput(KeyEvent event, Player player) {
        int keyCode = event.getKeyCode();
        float playerX = player.getX();
        float playerY = player.getY();
        float playerWidth = player.getWidth();
        float playerHeight = player.getHeight();
        int screenWidth = player.getScreenWidth();
        int screenHeight = player.getScreenHeight();

        // 한번에 이동할 거리 정의
        final float MOVE_DISTANCE = 50.0f;  // 픽셀 단위

        // 키보드 입력 처리 전 경계 체크
        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                // 왼쪽 이동 (A)
                if (keyCode == KeyEvent.KEYCODE_A && playerX > playerWidth / 2 + MOVE_DISTANCE) {
                    player.setX(playerX - MOVE_DISTANCE);
                    return true;
                }
                // 오른쪽 이동 (D)
                if (keyCode == KeyEvent.KEYCODE_D && playerX < screenWidth - playerWidth / 2 - MOVE_DISTANCE) {
                    player.setX(playerX + MOVE_DISTANCE);
                    return true;
                }
                // 위쪽 이동 (W)
                if (keyCode == KeyEvent.KEYCODE_W && playerY > playerHeight / 2 + MOVE_DISTANCE) {
                    player.setY(playerY - MOVE_DISTANCE);
                    return true;
                }
                // 아래쪽 이동 (S)
                if (keyCode == KeyEvent.KEYCODE_S && playerY < screenHeight - playerHeight / 2 - MOVE_DISTANCE) {
                    player.setY(playerY + MOVE_DISTANCE);
                    return true;
                }

                // 대각선 이동 구현 (동시에 두 키를 누른 경우를 위한 별도 처리)
                // 왼쪽 위 (A+W)
                if (keyCode == KeyEvent.KEYCODE_Q &&
                        playerX > playerWidth / 2 + MOVE_DISTANCE * 0.7f &&
                        playerY > playerHeight / 2 + MOVE_DISTANCE * 0.7f) {
                    player.setX(playerX - MOVE_DISTANCE * 0.7f);
                    player.setY(playerY - MOVE_DISTANCE * 0.7f);
                    return true;
                }
                // 오른쪽 위 (D+W)
                if (keyCode == KeyEvent.KEYCODE_E &&
                        playerX < screenWidth - playerWidth / 2 - MOVE_DISTANCE * 0.7f &&
                        playerY > playerHeight / 2 + MOVE_DISTANCE * 0.7f) {
                    player.setX(playerX + MOVE_DISTANCE * 0.7f);
                    player.setY(playerY - MOVE_DISTANCE * 0.7f);
                    return true;
                }
                // 왼쪽 아래 (A+S)
                if (keyCode == KeyEvent.KEYCODE_Z &&
                        playerX > playerWidth / 2 + MOVE_DISTANCE * 0.7f &&
                        playerY < screenHeight - playerHeight / 2 - MOVE_DISTANCE * 0.7f) {
                    player.setX(playerX - MOVE_DISTANCE * 0.7f);
                    player.setY(playerY + MOVE_DISTANCE * 0.7f);
                    return true;
                }
                // 오른쪽 아래 (D+S)
                if (keyCode == KeyEvent.KEYCODE_C &&
                        playerX < screenWidth - playerWidth / 2 - MOVE_DISTANCE * 0.7f &&
                        playerY < screenHeight - playerHeight / 2 - MOVE_DISTANCE * 0.7f) {
                    player.setX(playerX + MOVE_DISTANCE * 0.7f);
                    player.setY(playerY + MOVE_DISTANCE * 0.7f);
                    return true;
                }
                break;
        }
        return false;
    }

}
