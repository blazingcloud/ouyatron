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

public class Asteroid extends RenderObject {

    private int color;

    public Asteroid() {
        super(1.0f);
        
        // Pick a random starting location somewhere on an edge
        float randomStart = (float) (Math.random() * (GameRenderer.BOARD_WIDTH - 1.0f) + 1.0f);
        if (randomStart < 0.5f) {
        		translation.x = randomStart;
        		translation.y = 0;
        } else {
        		translation.x = 0;
        		translation.y = randomStart;
        }
        rotation = (float) (Math.random() * 360.0f);
        
        // Random starting location generates a flight direction
        float fwdX = (float) Math.sin(Math.toRadians(-rotation));
        float fwdY = (float) Math.cos(Math.toRadians(-rotation));
        float speed = (float) (Math.random() * 0.1f);
        this.flight.set(speed * fwdX, speed * fwdY);
        
        // Pick a random starting omega (which is the standard for rotational velocity)
        omega = (float) (Math.random() * 5.0f - 2.5f);
        
        // Pick a random size and set the radius
        radius = (float) (Math.random() + 0.25f);
        scale.x = radius;
        scale.y = scale.x + (float) (Math.random() * 0.4f - 0.2f);
        
        // Set a color
        color = Color.GRAY;

        setCollisionListener(new CollisionListener() {
            @Override
            public void onCollide(PointF prev, RenderObject me, RenderObject other) {
                if (other instanceof Player) {
                    Player p = (Player) other;
                    p.doBurst();
                    p.die();
                    Asteroid.this.destroy();
                    new Asteroid();
                } else if (other instanceof Wall || other instanceof Bullet) {
                    Asteroid.this.destroy();
                }
            }
        });
    }
    
    void spawnedFrom(Asteroid a) {
    		translation.x = a.translation.x;
    		translation.y = a.translation.y;
    		radius = 0.5f * a.radius;
    		scale.x = radius;
        scale.y = scale.x + (float) (Math.random() * 0.4f - 0.2f);
    }
    
    @Override
    protected void initModel() {
        final short[] _indicesArray = {
        		0, 1, 2,
        		0, 2, 3,
        		0, 3, 4, 
        		0, 4, 5,
        		0, 5, 6,
        		0, 6, 7,
        		0, 7, 8,
        		0, 8, 9,
        		0, 9, 10,
        		0, 10, 11,
        		0, 11, 12};

        // float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(_indicesArray.length * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();

        // short has 2 bytes
        ByteBuffer ibb = ByteBuffer.allocateDirect(_indicesArray.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();

        final float[] coords = {
                0.0f,  1.0f, 0.0f, // 0
                0.3f,  0.8f, 0.0f, // 1
                0.6f,  0.6f, 0.0f, // 2
                0.7f,  0.0f, 0.0f, // 3
                0.6f, -0.5f, 0.0f, // 4
                0.5f, -0.6f, 0.0f, // 5
                0.0f, -1.0f, 0.0f, // 6
               -0.4f, -1.0f, 0.0f, // 7
               -0.7f, -0.5f, 0.0f, // 8
               -1.2f, -0.1f, 0.0f, // 9
               -1.0f,  0.3f, 0.0f, // 10
               -0.7f,  0.7f, 0.0f, // 11
               -0.3f,  0.9f, 0.0f, // 12
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
    }
    
    @Override
    protected void doRender(GL10 gl) {
        setColor(gl, this.color);
        super.doRender(gl);
    }
    
    protected void asteroidDeath() {
    		if (radius > 0.3) {
	    	    long n = 3;
	    	    for (long i = 0; i < n; i++) {
	    	        Asteroid b = new Asteroid();
	    	        b.spawnedFrom(this);
	        		b.flight.x += flight.x;
	        		b.flight.y += flight.y;
	    	    }
    		}
    }
}
