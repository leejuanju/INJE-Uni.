package com.example.scrollingshooter_3;


import java.util.List;

public class CollisionDetector {
    public boolean checkCollision(GameObject obj1, GameObject obj2) {
        // RectF.intersect()를 사용하여 히트박스 충돌 체크
        return obj1.getHitBox().intersect(obj2.getHitBox());
    }

    public void detectCollisions(Player player, List<Enemy> enemies,
                                 List<Projectile> playerProjectiles,
                                 List<Projectile> enemyProjectiles,
                                 List<Explosion> explosions,
                                 Boss boss,
                                 GameView gameView) {
        // 플레이어 미사일과 적/보스 충돌 체크
        for (int i = playerProjectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = playerProjectiles.get(i);
            boolean hit = false;

            // 일반 적과 충돌 체크
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);

                if (checkCollision(projectile, enemy)) {
                    enemy.takeDamage(projectile.getDamage());
                    hit = true;

                    if (enemy.isDestroyed()) {
                        explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                        enemies.remove(j);
                        // 점수 및 처치 수 증가
                        gameView.increaseScore(10);
                        gameView.increaseEnemiesDefeated();
                    }
                    break;
                }
            }

            // 보스와 충돌 체크
            if (!hit && boss != null && checkCollision(projectile, boss)) {
                boss.takeDamage(projectile.getDamage());
                hit = true;

                // 폭발 효과 생성
                explosions.add(new Explosion(projectile.getX(), projectile.getY(), false));

                if (boss.isDestroyed()) {
                    // 큰 폭발 효과
                    explosions.add(new Explosion(boss.getX(), boss.getY(), true));

                    // 레벨 증가 및 다음 라운드 준비
                    gameView.defeatedBoss();
                }
            }

            if (hit) {
                playerProjectiles.remove(i);
            }
        }

        // 적 미사일과 플레이어 충돌 체크
        for (int i = enemyProjectiles.size() - 1; i >= 0; i--) {
            Projectile projectile = enemyProjectiles.get(i);

            if (checkCollision(projectile, player)) {
                player.takeDamage(projectile.getDamage());
                enemyProjectiles.remove(i);
            }
        }

        // 적과 플레이어 직접 충돌 체크
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (checkCollision(enemy, player)) {
                player.takeDamage(30); // 직접 충돌은 큰 데미지
                explosions.add(new Explosion(enemy.getX(), enemy.getY()));
                enemies.remove(i);
                break;
            }
        }

        // 보스와 플레이어 직접 충돌 체크
        if (boss != null && checkCollision(boss, player)) {
            player.takeDamage(50); // 보스와 충돌은 매우 큰 데미지
            explosions.add(new Explosion(player.getX(), player.getY()));
        }
    }
}