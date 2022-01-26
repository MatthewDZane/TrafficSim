package entities;

import java.awt.Rectangle;

import helpers.AABB;

public abstract class Entity {
	private AABB boundingBox;
	protected Rectangle position;
	
	protected double speed = 1;
	
	public AABB getBoundingBox() { return boundingBox; }
	public Rectangle getPosition() { return position; }
	
	public double getSpeed() { return speed; }
	
	public Entity(Rectangle positionIn) {
		boundingBox = new AABB(positionIn);
		position = positionIn;
	}
}
