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
import tv.ouya.console.api.OuyaController;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Player extends RenderObject {
    private int playerNum = -1;
    private int deviceId = -1;
    private boolean isDead = false;
    private long lastShotTime = 0;
    private long lastDeadTime = 0;
    final private float c_thrustPower = 0.1f;

    private PointF shootDir;
    private float forwardAmount;

    static final private int[] c_playerColors = {
            Color.CYAN,
            Color.RED,
            Color.YELLOW,
            Color.GREEN
    };
    static final private int c_deadColor = Color.DKGRAY;
    static final private float c_playerRadius = 0.5f;
    static final private float c_timeBetweenShots = 0.1f;
    static final private float c_maxSpeed = 1.0f;

    public Player(int playerNum) {
        super(c_playerRadius);
        this.playerNum = playerNum;
        shootDir = new PointF();

        setCollisionListener(new CollisionListener() {
            @Override
            public void onCollide(PointF prev, RenderObject me, RenderObject other) {
                if (other instanceof Wall) {
                    Wall wall = (Wall) other;

                    translation = wall.slideAgainst(prev, translation, getRadius());
                }
            }
        });
    }

    public void init(int deviceId) {
        this.deviceId = deviceId;

        // Pick a random starting location
        translation.x = (float) (Math.random() * (GameRenderer.BOARD_WIDTH - 1.0f) + 1.0f);
        translation.y = (float) (Math.random() * (GameRenderer.BOARD_HEIGHT - 1.0f) + 1.0f);
        rotation = (float) (Math.random() * 360.0f);
    }

    public boolean isValid() {
        return (deviceId >= 0);
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void shoot(float dirX, float dirY) {
        shootDir.set(dirX, dirY);
    }

    public void die() {
        isDead = true;
        this.flight.set(0.0f, 0.0f);
        lastDeadTime = System.currentTimeMillis();
    }

    @Override
    protected void initModel() {
        final short[] _indicesArray = {
        		
        		0, 	1, 2, //(left circle)
        		0, 	2, 3,	
        		0, 	3, 4,    			    
        		0, 	4, 5,    
        		0, 	5, 6,
        		0, 	6, 7,
        		0, 	19,	20, //(left circle)
        		0, 	20,	21,	
        		0, 	21,	22,    			    
        		0, 	22,	23,    
        		0, 	23, 	24,
        		0, 	24, 	25,
        		1,	19, 0,
        		7,	25, 0,
        		1,	19,	37,
        		7, 	17, 	26, //(center rect)
        		7, 	35, 	25,
        		16,	9,	27, //(strut)
        		27, 	34, 	16,
        		12,	11, 10, //(left engine)
        		12,	13, 15,
        		12,	10, 15,
        		13,	15, 14,
        		30,	29, 28, //(right engine)
        		30,	31, 33,
        		30,	28, 33,
        		31,	33, 32,
        		17,	35, 18,
        };
        	
//        	{0, 1, 2, 1, 3, 2};

        // float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(_indicesArray.length * 3 * 4);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();

        // short has 2 bytes
        ByteBuffer ibb = ByteBuffer.allocateDirect(_indicesArray.length * 2);
        ibb.order(ByteOrder.nativeOrder());
        indexBuffer = ibb.asShortBuffer();

        final float[] coords = {
        		0.0f,    0.0f,   0.0f,		//0
        	    -0.5f,    0.866f, 0.0f,		//1
        		-0.707f,  0.707f, 0.0f,		//2
        		-0.866f,  0.5f,   0.0f,		//3
        		-1.0f,    0.0f,   0.0f,		//4
        		-0.866f, -0.5f,   0.0f,		//5
        		-0.707f, -0.707f, 0.0f,		//6
        		-0.5f,   -0.866f, 0.0f,		//7
        		-0.5f,   -1.2f,   0.0f,		//8
        		-0.707f, -1.2f,   0.0f,		//9
        		-0.707f, -1.0f,   0.0f,		//10
        		-0.866f, -0.866f, 0.0f,		//11
        		-1.025f, -1.0f,   0.0f,		//12
        		-1.025f, -1.7f,   0.0f,		//13
        		-0.866f, -1.834f, 0.0f,		//14
        		-0.707f, -1.7f,   0.0f,		//15
        		-0.707f, -1.4f,   0.0f,		//16
        		-0.5f,   -1.4f,   0.0f,		//17
        		-0.3f,   -1.5f,   0.0f,		//18
        		0.5f, 	  0.866f, 0.0f,		//19	r1
        		0.707f, 	  0.707f, 0.0f,		//	20	r2					
        		0.866f, 	  0.5f,   0.0f,		//21	r3
        		1.0f, 	  0.0f,   0.0f,		//	22	r4
        		0.866f, 	 -0.5f,   0.0f,		//		23	r5
        		0.707f, 	 -0.707f, 0.0f,		//	24	r6
        		0.5f, 	 -0.866f, 0.0f,		//		25	r7
        		0.5f, 	 -1.2f,   0.0f,		//		26	r8
        		0.707f,	 -1.2f,   0.0f,		//		27	r9
        		0.707f,	 -1.0f,   0.0f,		//		28	r10
        		0.866f,	 -0.866f, 0.0f,		//29	r11
        		1.025f,	 -1.0f,   0.0f,		//		30	r12
        		1.025f, 	 -1.7f,   0.0f,		//		31	r13
        		0.866f,	 -1.834f, 0.0f,		//32	r14
        		0.707f, 	 -1.72f,   0.0f,		//		33	r15
        		0.707f,	 -1.4f,   0.0f,		//		34	r16
        		0.5f,	 -1.4f,   0.0f,		//	35	r17
        		0.3f,	 -1.5f,   0.0f,		//	36	r18
        		0.0f,	  1.0f,	  0.0f,		//	37
            		};		
        
    				/*{
                -0.9f, -0.9f, 0.0f, // 0
                 0.0f, -0.2f, 0.0f, // 1
                 0.0f,  0.1f, 0.0f, // 2
                 0.9f, -0.9f, 0.0f, // 3
       			};*/
        
        

        vertexBuffer.put(coords);
        indexBuffer.put(_indicesArray);

        vertexBuffer.position(0);
        indexBuffer.position(0);
    }

    final float c_forwardSpeed = 0.1f;

    static private float stickMag(float axisX, float axisY) {
        float stickMag = (float) Math.sqrt(axisX * axisX + axisY * axisY);
        return stickMag;
    }

    static public boolean isStickNotCentered(float axisX, float axisY) {
        final float c_minStickDistance = 0.2f;
        float stickMag = stickMag(axisX, axisY);
        return (stickMag >= c_minStickDistance);
    }

    private void getForwardAmountFromController(OuyaController c) {
        float axisX = c.getAxisValue(OuyaController.AXIS_LS_X);
        axisX = Math.min(axisX, 1.0f);
        float axisY = c.getAxisValue(OuyaController.AXIS_LS_Y);
        axisY = Math.min(axisY, 1.0f);
        if (isStickNotCentered(axisX, axisY)) {
            float stickMag = stickMag(axisX, axisY);
            float desiredDir = (float) Math.toDegrees( Math.atan2(-axisX, axisY) );
            setRotate(desiredDir);
            forwardAmount = stickMag * c_forwardSpeed;
        } else {
            forwardAmount = 0.0f;
        }
    }

    private void getShootDirFromController(OuyaController c) {
        float axisX = c.getAxisValue(OuyaController.AXIS_RS_X);
        axisX = Math.min(axisX, 1.0f);
        float axisY = c.getAxisValue(OuyaController.AXIS_RS_Y);
        axisY = Math.min(axisY, 1.0f);
        if (isStickNotCentered(axisX, axisY)) {
            float stickMag = stickMag(axisX, axisY);
            // normalize the direction vec
            shootDir.x = axisX / stickMag;
            shootDir.y = axisY / stickMag;
        } else {
            shootDir.set(0.0f, 0.0f);

            // Stick isn't pressed, check the buttons
            if (c.getButton(OuyaController.BUTTON_O)
                   || c.getButton(OuyaController.BUTTON_U)) {
                PointF fwdVec = getForwardVector();
                shootDir = fwdVec;
            }
            
            if (c.getButton(OuyaController.BUTTON_Y)
            		   || c.getButton(OuyaController.BUTTON_R2)) {
            		PointF fwdVec = getForwardVector();
                shootDir = fwdVec;
            }
        }
    }
    
    private void thrust(float amount) {
    		PointF direction = getForwardVector();
    		flight.x += direction.x * c_thrustPower * amount;
    		flight.y += direction.y * c_thrustPower * amount;
    }
    
    @Override
    protected void update() {
        if (!isValid()) {
            return;
        }
        
        super.update();
        
        OuyaController c = OuyaController.getControllerByDeviceId(deviceId);
        getForwardAmountFromController(c);
        getShootDirFromController(c);
        	
        long currentTime = System.currentTimeMillis();
        
        if (isDead) {
            float timeSinceDead = (currentTime - lastDeadTime) / 1000.0f;
            if (timeSinceDead > 5.0f) {
            		isDead = false;
            }
        } else {
        		thrust(forwardAmount);
            drift();
            float l = flight.length();
    			if (l > c_maxSpeed){
    				flight.x = flight.x*c_maxSpeed/l;
    				flight.y = flight.y*c_maxSpeed/l;
    			}
        }
        
        float timeSinceLastShot = (currentTime - lastShotTime) / 1000.0f;
        
        if (c.getButton(OuyaController.BUTTON_Y)) {
	        	if (timeSinceLastShot > c_timeBetweenShots * 5) {
	        	    final float c_bulletDistance = 0.0f;
	        	    long n = 10;
	        	    for (long i = 0; i < n; i++) {
	        	    	    double theta = i * 2.0 * Math.PI / n;
	        	        Bullet b = new Bullet(this,
	        	        		(float) (translation.x + Math.cos(theta) * c_bulletDistance),
	        	        		(float) (translation.y + Math.sin(theta) * c_bulletDistance),
	        	        		(float) Math.toDegrees(theta), Color.GREEN );
	        	        		b.flight.x += flight.x;
	        	        		b.flight.y += flight.y;
	        	    }
	        	}
        }
        
        if (shootDir.x != 0.0f || shootDir.y != 0.0f) {
            if (timeSinceLastShot > c_timeBetweenShots) {
                lastShotTime = currentTime;
                float desiredDir = (float) Math.toDegrees( Math.atan2(-shootDir.x, shootDir.y) );

                final float c_bulletDistance = 0.0f;
                Bullet b = new Bullet(this, translation.x + shootDir.x * c_bulletDistance, translation.y + shootDir.y * c_bulletDistance, desiredDir, c_playerColors[playerNum]);
                b.flight.x += flight.x;
    	        		b.flight.y += flight.y;
            }
        }
    }

    @Override
    protected void doRender(GL10 gl) {
        if (!isValid()) {
            return;
        }

        int color = isDead ? c_deadColor : c_playerColors[playerNum];
        setColor(gl, color);
        super.doRender(gl);
    }

    @Override
    public boolean doesCollide(RenderObject other) {
        if (other instanceof Player) {
            return false;
        }
        return super.doesCollide(other);
    }
    
    public void doBurst(){
	    final float c_bulletDistance = 0.0f;
	    long n = 10;
	    for (long i = 0; i < n; i++) {
	    	    double theta = i * 2.0 * Math.PI / n;
	        Bullet b = new Bullet(this,
	        		(float) (translation.x + Math.cos(theta) * c_bulletDistance),
	        		(float) (translation.y + Math.sin(theta) * c_bulletDistance),
	        		(float) Math.toDegrees(theta), Color.GREEN );
	        		b.flight.x += flight.x;
	        		b.flight.y += flight.y;
	    }
    }
   }
