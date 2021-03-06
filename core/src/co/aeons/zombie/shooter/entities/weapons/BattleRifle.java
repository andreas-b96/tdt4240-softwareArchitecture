package co.aeons.zombie.shooter.entities.weapons;

import java.util.ArrayList;
import java.util.LinkedList;

import co.aeons.zombie.shooter.entities.bullets.BRBullet;
import co.aeons.zombie.shooter.entities.bullets.Bullet;
import co.aeons.zombie.shooter.managers.Jukebox;
import co.aeons.zombie.shooter.managers.ResourceManager;

public class BattleRifle extends Weapon {

    private static final float RELOAD_TIME = 1.5f;
    private static final float FIRE_RATE = 0.25f;

    private int bulletDelay;

    public BattleRifle(float x, float y) {
        super(x, y);

        clipSize = 36;
        fireRate = FIRE_RATE;

        texturePath = "weapons/assault1.png";
        weaponTexture = ResourceManager.getBattleRifleTexture();
        bullets = new LinkedList<Bullet>();
        reload();
        isReloading = false;
        bulletDelay = 0;
    }

    @Override
    public ArrayList<Bullet> shoot() {
        ArrayList<Bullet> output = new ArrayList<Bullet>();
        if(!isReloading && !isFired){
            if(!bullets.isEmpty()) {
                for (int i = 0; i < 3; i++) {
                    output.add(bullets.poll());
                }
                playSound();
                if(bullets.isEmpty()) {
                    reload();
                    Jukebox.play("brReload");
                }
                isFired = true;
                fireRate = FIRE_RATE;
            }else reload();
        }
        return output;
    }

    @Override
    public void reload() {
        bullets.clear();
        isReloading = true;
        reloadTime = RELOAD_TIME;
        for (int i = 0; i < clipSize; i++) {
            setBulletDelay(i % 3);
            bullets.add(getNewBullet());
        }
    }

    @Override
    public void playSound() {
        Jukebox.play("drrr");
    }

    @Override
    public Bullet getNewBullet() {
        return new BRBullet(this.x, this.y, this.bulletDelay);
    }

    public void setBulletDelay(int bulletDelay) {
        this.bulletDelay = bulletDelay;
    }

    public String getType(){
        return "br";
    }
}
