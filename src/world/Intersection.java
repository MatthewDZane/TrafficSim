package world;

import java.awt.Rectangle;
import java.util.ArrayList;

import entities.Entity;

public class Intersection {
	private Rectangle position;
	private boolean hasStopSign;
	private ArrayList<Entity> entities = new ArrayList<Entity>();
	
	public Rectangle getPosition() { return position; }
	public boolean hasStopSign() { return hasStopSign; }
	public ArrayList<Entity> getEntities() { return entities; }
	
	public Intersection(Rectangle positionIn, boolean hasStopSignIn) {
		position = positionIn;
		hasStopSign = hasStopSignIn;
	}
	
	public void addEntity(Entity entity) {
		if (!entities.contains(entity)) {
			entities.add(entity);
		}
	}
}
