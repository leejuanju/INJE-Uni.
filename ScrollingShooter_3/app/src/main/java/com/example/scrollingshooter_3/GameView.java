package com.example.scrollingshooter_3;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameLoop gameLoop;
    private Player player;
    private List<Enemy> enemies;
    private List<Projectile> playerProjectiles;
    private List<Projectile> enemyProjectiles;
    private List<Explosion> explosions;
    private InputHandler inputHandler;

    private int screenWidth;
    private int screenHeight;
    private int score;
    private boolean gameOver;
    private long lastEnemySpawnTime;
    private static final long ENEMY_SPAWN_INTERVAL = 1000; // 1초마다 적 생성

    private Paint textPaint;
    private Paint backgroundPaint;

    private CollisionDetector collisionDetector;

    // GameView 클래스에 보스 변수와 레벨 변수 추가
    private Boss boss;
    private int level = 1;
    private int enemiesDefeated = 0;
    private static final int ENEMIES_TO_SPAWN_BOSS = 30; // 보스 등장 전 처치할 적 수

    private Context context;

    private Bitmap backgroundImage;

    private int currentLevel = 1;


    // GameView 클래스에 상태 상수 추가
    private static final int STATE_GAMEPLAY = 0;
    private static final int STATE_GAMEOVER = 1;
    private int gameState = STATE_GAMEPLAY;

    // 게임오버 관련 UI 요소
    private Paint gameOverTextPaint;
    private Rect restartButtonRect;
    private Paint buttonPaint;
    

    // 레벨에 따라 배경 이미지 변경 메서드
    private void updateBackgroundForLevel(int level) {
        // 이전 비트맵 메모리 해제
        if (backgroundImage != null && !backgroundImage.isRecycled()) {
            backgroundImage.recycle();
        }

        // 레벨에 따라 다른 이미지 로드
        int backgroundResource;
        switch (level % 4) {
            case 0:
                backgroundResource = R.drawable.sunnone;
                break;
            case 1:
                backgroundResource = R.drawable.skysun;
                break;
            case 2:
                backgroundResource = R.drawable.skysundown;
                break;
            case 3:
                backgroundResource = R.drawable.skynight;
                break;
            default:
                backgroundResource = R.drawable.skynight;
        }

        // 새 배경 이미지 로드 및 크기 조정
        backgroundImage = BitmapFactory.decodeResource(getResources(), backgroundResource);
        backgroundImage = Bitmap.createScaledBitmap(
                backgroundImage,
                getWidth(),
                getHeight(),
                false
        );
    }

    public GameView(Context context) {
        super(context);
        this.context = context; // Context 저장
        getHolder().addCallback(this);

        // 배경 이미지 한 번만 로드 (중요!)
        backgroundImage = BitmapFactory.decodeResource(getResources(), R.drawable.skysun);

        // 화면 크기에 맞게 이미지 크기 조정
        backgroundImage = Bitmap.createScaledBitmap(
                backgroundImage,
                getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels,
                true
        );

        // 입력 핸들러 초기화
        inputHandler = new InputHandler();

        // 텍스트 페인트 초기화
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(50);


        collisionDetector = new CollisionDetector();


        // 배경 페인트 초기화
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.rgb(0, 153, 255)); // 이미지의 파란 배경색

        setFocusable(true);


        //게임오버시
        gameOverTextPaint = new Paint();
        gameOverTextPaint.setColor(Color.RED);
        gameOverTextPaint.setTextSize(80);
        gameOverTextPaint.setTextAlign(Paint.Align.CENTER);

        buttonPaint = new Paint();
        buttonPaint.setColor(Color.BLUE);

    }



    private void initGame() {
        // 화면 크기 구하기
        screenWidth = getWidth();
        screenHeight = getHeight();

        // 게임 객체 초기화
        player = new Player();
        player.setInfo(context, screenWidth / 2, screenHeight * 0.85f, screenWidth, screenHeight);

        enemies = new ArrayList<>();
        playerProjectiles = new ArrayList<>();
        enemyProjectiles = new ArrayList<>();
        explosions = new ArrayList<>();

        boss = null;
        enemiesDefeated = 0;
        level = 1;

        // 게임 상태 초기화
        score = 0;
        gameOver = false;
        lastEnemySpawnTime = System.currentTimeMillis();

        // 초기 배경 이미지 설정
        updateBackgroundForLevel(level);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameLoop = new GameLoop(this, holder);
        initGame();
        gameLoop.setRunning(true);
        gameLoop.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // 필요시 구현
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                gameLoop.setRunning(false);
                gameLoop.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }//백그라운드
        if (backgroundImage != null && !backgroundImage.isRecycled()) {
            backgroundImage.recycle();
            backgroundImage = null;}
    }

    public void update() {
        if (gameOver) return;

        // 플레이어 업데이트
        if (gameState == STATE_GAMEPLAY) {
            // 기존 게임 업데이트 코드
            player.update();
        player.fireIfReady(playerProjectiles);

        //보스 업데이트
        if (boss != null) {
            boss.update();
            boss.fireProjectiles(enemyProjectiles,level);
        } else {
            // 일정 수의 적을 처치하면 보스 등장
            if (enemiesDefeated >= ENEMIES_TO_SPAWN_BOSS) {
                spawnBoss();
            } else {
                // 적 생성 및 업데이트
                spawnEnemies();
            }
        }


        // 적 생성 및 업데이트
        spawnEnemies();
        updateEnemies();

        // 발사체 업데이트
        updateProjectiles();

        // 폭발 효과 업데이트
        updateExplosions();


        // 충돌 감지 (보스 추가)
        collisionDetector.detectCollisions(player, enemies, playerProjectiles,
                enemyProjectiles, explosions, boss,this);

        // 게임 오버 체크
        if (player.getHealth() <= 0) {
            gameOver = true;
        }
            //다시하기
            if (player.getHealth() <= 0) {
                gameState = STATE_GAMEOVER;
                // 다시하기 버튼 위치 계산
                restartButtonRect = new Rect(
                        screenWidth/2 - 150,
                        screenHeight/2 + 100,
                        screenWidth/2 + 150,
                        screenHeight/2 + 200
                );
            }
        }
    }
    // GameView.java에 추가할 메서드들
    public void increaseScore(int points) {
        score += points;
    }

    public void increaseEnemiesDefeated() {
        enemiesDefeated++;
    }

    public void defeatedBoss() {
        boss = null;
        level++;
        enemiesDefeated = 0;
        score += 100 * level;

        updateBackgroundForLevel(level);
    }
    private void spawnBoss() {
        boss = new Boss(getContext(),screenWidth / 2, screenHeight * 0.2f, screenWidth, level);
    }

    private void spawnEnemies() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEnemySpawnTime > ENEMY_SPAWN_INTERVAL) {
            lastEnemySpawnTime = currentTime;
            Random random = new Random();

            // 레벨에 따라 한 번에 생성되는 적의 수 증가
            int enemyCount = 1 + (level / 2); // 레벨 2마다 적 1개씩 추가

            for (int i = 0; i < enemyCount; i++) {
                float x = random.nextInt(screenWidth - 100) + 50;
                enemies.add(new Enemy(getContext(), x, -50 - (i * 50), screenWidth, screenHeight));
            }
        }
    }

    private void updateEnemies() {
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy enemy = it.next();
            enemy.update();

            // 화면 밖으로 나간 적은 제거
            if (enemy.getY() > screenHeight + 100) {
                it.remove();
            }

            // 일정 확률로 총알 발사
            if (Math.random() < 0.01) {
                enemy.fire(enemyProjectiles,level);
            }
        }
    }

    private void updateProjectiles() {
        // 플레이어 발사체 업데이트
        Iterator<Projectile> it = playerProjectiles.iterator();
        while (it.hasNext()) {
            Projectile projectile = it.next();
            projectile.update();
            if (!projectile.isVisible()) {
                it.remove();
            }
        }

        // 적 발사체 업데이트
        it = enemyProjectiles.iterator();
        while (it.hasNext()) {
            Projectile projectile = it.next();
            projectile.update();
            if (!projectile.isVisible()) {
                it.remove();
            }
        }
    }

    private void updateExplosions() {
        Iterator<Explosion> it = explosions.iterator();
        while (it.hasNext()) {
            Explosion explosion = it.next();
            explosion.update();
            if (explosion.isFinished()) {
                it.remove();
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            // 배경 그리기
            //canvas.drawColor(backgroundPaint.getColor());
            //canvas.drawColor(Color.rgb(0, 153, 255)); // 이미지의 파란색 배경
            //canvas.drawBitmap(backgroundImage, 0, 0, null);
            // 배경 이미지 그리기
            if (backgroundImage != null) {
                canvas.drawBitmap(backgroundImage, 0, 0, null);
            } else {
                // 배경 이미지가 없으면 단색 배경
                canvas.drawColor(Color.rgb(0, 153, 255));
            }

            if (gameState == STATE_GAMEPLAY) {
                // 점수 표시
                canvas.drawText("Score: " + score, 50, 50, textPaint);
                canvas.drawText("Level: " + level, 50, 100, textPaint);

                // 플레이어 그리기
                player.draw(canvas);

                if (boss != null) {
                    boss.draw(canvas);
                }
                // 적 그리기
                for (Enemy enemy : enemies) {
                    enemy.draw(canvas);
                }

                // 발사체 그리기
                for (Projectile projectile : playerProjectiles) {
                    projectile.draw(canvas);
                }

                for (Projectile projectile : enemyProjectiles) {
                    projectile.draw(canvas);
                }

                // 폭발 효과 그리기
                for (Explosion explosion : explosions) {
                    explosion.draw(canvas);
                }
            } else if (gameState == STATE_GAMEOVER) {
                // 게임오버 화면 그리기
                canvas.drawText("GAME OVER", screenWidth/2, screenHeight/2 - 100, gameOverTextPaint);
                canvas.drawText("Score: " + score, screenWidth/2, screenHeight/2, gameOverTextPaint);

                // 다시하기 버튼
                canvas.drawRect(restartButtonRect, buttonPaint);
                Paint buttonTextPaint = new Paint(gameOverTextPaint);
                buttonTextPaint.setTextSize(40);
                buttonTextPaint.setColor(Color.WHITE);
                canvas.drawText("RESTART", screenWidth/2, screenHeight/2 + 160, buttonTextPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameState == STATE_GAMEPLAY) {
            // 기존 게임플레이 터치 처리
            return inputHandler.handleTouchInput(event, player);
        } else if (gameState == STATE_GAMEOVER) {
            // 게임오버 화면에서의 터치 처리
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float touchX = event.getX();
                float touchY = event.getY();

                // 다시하기 버튼 클릭 감지
                if (restartButtonRect.contains((int)touchX, (int)touchY)) {
                    // 게임 초기화
                    initGame();
                    gameState = STATE_GAMEPLAY;
                    gameOver = false;
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return inputHandler.handleKeyInput(event, player) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return inputHandler.handleKeyInput(event, player) || super.onKeyUp(keyCode, event);
    }

    public void pause() {
        if (gameLoop != null) {
            gameLoop.setRunning(false);
        }
    }

    public void resume() {
        if (gameLoop != null) {
            gameLoop.setRunning(true);
        }
    }
}