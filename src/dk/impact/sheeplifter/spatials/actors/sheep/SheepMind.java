package dk.impact.sheeplifter.spatials.actors.sheep;

import com.jme.math.Vector3f;
import com.jme.util.Timer;

import dk.impact.sheeplifter.spatials.World;

public class SheepMind {
	private SheepBody		body;
	private World			world;
	private Timer			timer;
	
	private	float			endEscapeAngle;
	private	float			endEscapeAngleSpan;
	private float 			endEscapeTime;
	
	private float			nextMovementTime;
	private SheepBody.Turn	nextTurnType;
	private float			nextTurnAmount;
	
	private float			volcanoToCenterLimit;
	
	enum State {
		SearchingForWaterAndVolcano,
		Turning,
		EscapingWater
	}
	
	private State		state;
	
	public SheepMind(SheepBody body, World world) {
		this.body = body;
		this.world = world;
		this.timer = Timer.getTimer();
		
		state = State.SearchingForWaterAndVolcano;
		
        volcanoToCenterLimit = world.getXZDistanceVolcanoToCenter() * 1.3f;		
        
        generateNextMovement();
	}
	
	private float correctAngle(float angle) {
		if (angle > Math.PI) {
			return (float) (angle - 2f * Math.PI);
		}
		else if (angle < -Math.PI) {
			return (float) (angle + 2f * Math.PI);
		}
		
		return angle;
	}
	
	private boolean isInRange(float angle, float targetAngle, float targetSpan) {
		float	minAngle = correctAngle(targetAngle - targetSpan);
		float	maxAngle = correctAngle(targetAngle + targetSpan);
		boolean	wrap = false;
		
		wrap = (minAngle > maxAngle);
		
		if (wrap) {
			return (angle > minAngle || angle < maxAngle);
		}
		else {
			return (angle > minAngle && angle < maxAngle);			
		}
	}

	protected void generateNextMovement() {
		nextMovementTime = timer.getTimeInSeconds() + 7f + (float) Math.random() * 7f;		
		
		int rndTurnType = (int) (Math.random() * SheepBody.Turn.values().length);		
		nextTurnType = SheepBody.Turn.values()[rndTurnType];
		
		nextTurnAmount = (float) Math.random() * 0.7f;
	}
	
	
    public void update() {
    	if (state == State.SearchingForWaterAndVolcano) {
        	float samplingDistanceFromSheep = 15f;

        	Vector3f position = body.getWorldTranslation();
        	Vector3f orientation = body.getLocalRotation().getRotationColumn(0);

        	orientation.multLocal(samplingDistanceFromSheep);
        	
            World.SurfacePoint surfacePoint = world.getSurfacePointAt(position.add(orientation.getX(), 0, orientation.getZ()));    	
        	
            float distanceToCenter = world.getXZDistanceToCenter(position);
            
            if ((surfacePoint.getType() == World.SurfaceType.Ocean) || (distanceToCenter < volcanoToCenterLimit)) {
    			if (Math.random() > 0.5) {
        			body.turnLeft(0.5f + (float) Math.random());
    			} 
    			else {
        			body.turnRight(0.5f + (float) Math.random());
    			}
    			
    			if (body.getCurrentPresence() == SheepBody.Presence.OnLand) {
        			body.stopMotion();            	    				
    			}

    			state = State.Turning;
            	
            	endEscapeAngle = correctAngle(body.getHeading() + (float) Math.PI);
            	endEscapeAngleSpan = 0.3f + (float) (0.4f * Math.random());
            } else if (timer.getTimeInSeconds() > nextMovementTime) {
            	switch (nextTurnType) {
	            	case Left:
	            		body.turnLeft(nextTurnAmount);
	            		break;
	            	case Right:
	            		body.turnRight(nextTurnAmount);
	            		break;
	            	case No:
	            		body.stopTurning();
	            		break;
            	}
            	
            	generateNextMovement();
            }
    	}
    	else if (state == State.Turning) {
    		if (isInRange(body.getHeading(), endEscapeAngle, endEscapeAngleSpan)) {
    			//System.out.println("in range");
    			
            	body.goForward();
            	body.stopTurning();
    			
    			state = State.EscapingWater;
            	endEscapeTime = timer.getTimeInSeconds() + 7f + (float) Math.random() * 7f;
            }
    	}
    	else {
    		if (timer.getTimeInSeconds() > endEscapeTime) {
    			state = State.SearchingForWaterAndVolcano;
    			generateNextMovement();
    		}
    	}
    }
    
    public String toString() {
    	return state.toString();
    }
}
