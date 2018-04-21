package co.aeons.zombie.shooter.entities.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Queue;

import co.aeons.zombie.shooter.entities.bullets.Bullet;

public abstract class Weapon {

    protected Texture weaponTexture;
    protected String texturePath;
    protected float fireRate;
    protected boolean isFired;

    protected float x;
    protected float y;
    protected float width;
    protected float height;

    public Weapon(float x, float y) {
        this.x = x;
        this.y = y;
        isFired = false;
    }

    public abstract ArrayList<Bullet> shoot();

    public abstract void playSound();

    public abstract Bullet getNewBullet();

    public void update(float dt) {
        fireRate -= dt;

        if(fireRate <= 0) {
            isFired = false;
        }

    }

    public void draw(SpriteBatch sb){
        sb.begin();
        sb.draw(weaponTexture, x, y, width, height);
        sb.end();
    }

    public String getTexturePath() {
        return texturePath;
    }
}
