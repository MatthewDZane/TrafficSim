package helpers;

import java.awt.Rectangle;

public class Collision {
	public Rectangle distance;
	public boolean isIntersecting;
	
	public Collision(Rectangle distanceIn, boolean intersects) {
		distance = distanceIn;
		isIntersecting = intersects;
	}
	
	public int getDiagonalSquared() {
		return (int) (Math.pow(distance.width, 2) + Math.pow(distance.height, 2));
	}
}
