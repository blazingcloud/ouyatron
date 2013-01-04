/*
 * Copyright (C) 2012 OUYA, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.ouya.sample.game;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Bullet extends RenderObject {
    static private final float c_bulletRadius = 0.25f;
    static private final float c_bulletSpeed = 0.40f;

    static private final float c_bulletLifetime = 0.5f;
    private long startTime;
    private Player shooter;
    private int color;

    public Bullet(Player shooter, float translationX, float translationY, float rotation, int color) {
        super(c_bulletRadius);
        this.translation.set(translationX, translationY);
        this.rotation = rotation;
        this.color = color;
        this.shooter = shooter;
        
        float fwdX = (float) Math.sin(Math.toRadians(-rotation));
        float fwdY = (float) Math.cos(Math.toRadians(-rotation));
        this.flight.set(c_bulletSpeed * fwdX, c_bulletSpeed * fwdY);
        	
        startTime = System.currentTimeMillis();

        setCollisionListener(new CollisionListener() {
            @Override
            public void onCollide(PointF prev, RenderObject me, RenderObject other) {
                if (other instanceof Player && other != Bullet.this.shooter) {
                    Player p = (Player) other;
                    p.doBurst();
                    p.die();
                    Bullet.this.destroy();
                } else if (other instanceof Wall) {
                    Bullet.this.destroy();
                } else if (other instanceof Asteroid) {
                		Asteroid a = (Asteroid)other;
                		Bullet.this.destroy();
                		a.asteroidDeath();
                		a.destroy();
                }
            }
        });
    }

    @Override
    protected void initModel() {
        final short[] _indicesArray = {0, 1, 2, 0, 2, 3};

        // float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(_indicesArray.length * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();

        // short has 2 bytes
        ByteBuffer ibb = ByteBuffer.allocateDirect(_indicesArray.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();

        final float[] coords = {
                0.0f, -0.1f, 0.0f, // 0
                0.1f,  0.0f, 0.0f, // 1
                0.0f,  0.6f, 0.0f, // 2
               -0.1f,  0.0f, 0.0f, // 3
        };

        vertexBuffer.put(coords);
        indexBuffer.put(_indicesArray);

        vertexBuffer.position(0);
        indexBuffer.position(0);
    }

    

    @Override
    protected void update() {
        super.update();
        drift();

        long currentTime = System.currentTimeMillis();
        float elapsedTime = (currentTime - startTime) / 1000.0f;
        if (elapsedTime >= c_bulletLifetime) {
            destroy();
        }
    }

    @Override
    protected void doRender(GL10 gl) {
        setColor(gl, this.color);
        super.doRender(gl);
    }
}
