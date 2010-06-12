package dk.impact.sheeplifter.spatials;

import java.util.Vector;

import com.jme.scene.Spatial;

public class CollisionManager {
	class CollisionCheck {
		private Spatial					source;
		private Spatial					target;
		private CollisionEventInterface	event;
		
		public CollisionCheck(Spatial source, Spatial target, CollisionEventInterface event) {
			this.source = source;
			this.target = target;
			this.event = event;
		}

		public Spatial getSource() {
			return source;
		}

		public Spatial getTarget() {
			return target;
		}

		public CollisionEventInterface getEvent() {
			return event;
		}
	} 

	private Vector<CollisionCheck>	collisionChecks;
	
	public CollisionManager() {
		collisionChecks = new Vector<CollisionCheck>();
	}
	
	public void addCheck(Spatial source, Spatial target, CollisionEventInterface eventHandler) {
        collisionChecks.add(new CollisionCheck(source, target, eventHandler));
	}
	
	public void removeCheck(Spatial source, Spatial target) {
		for(int i = collisionChecks.size() - 1; i >= 0 ; i--) {
			if ((collisionChecks.get(i).getSource() == source) && (collisionChecks.get(i).getTarget() == target)) {
				collisionChecks.remove(i);
			}
		}
	}
	
	public void removeChecksInvolving(Spatial spatial) {
		for(int i = collisionChecks.size() - 1; i >= 0; i--) {
			if ((collisionChecks.get(i).getSource() == spatial) || (collisionChecks.get(i).getTarget() == spatial)) {
				collisionChecks.remove(i);
			}
		}
	}

	public void removeAllChecks() {
		collisionChecks.removeAllElements();
	}
	
	public boolean handleCollisions() {
        boolean		collisionHandled = false;
		
		for(int i = 0; i < collisionChecks.size(); i++) {
			collisionHandled |= handleCollision(collisionChecks.get(i));
		}
		
		return collisionHandled;
	}
	
	protected boolean handleCollision(CollisionCheck collisionCheck) {
        boolean		collisionHandled = false;
		
		while(collisionCheck.getSource().hasCollision(collisionCheck.getTarget(), false)) {
			collisionHandled |= collisionCheck.getEvent().spatialsCollided(collisionCheck.getSource(), collisionCheck.getTarget());
		}
		
		return collisionHandled;
	}
}
