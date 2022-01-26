package helpers;

import java.awt.Rectangle;

public class AABB {
	public Rectangle position;
	
	public AABB(Rectangle positionIn) {
		position = positionIn;
	}
	
	public Collision getCollision(AABB box2) {
		Rectangle intersection = position.intersection(box2.position);
		
		return new Collision(intersection, (intersection.width > 0 && intersection.height > 0));
	}
	
	public Direction correctPosition(AABB box2, Collision data) {
		Rectangle correction = data.distance;
		if (data.distance.width < data.distance.height) {
			if (correction.x > box2.position.x) {
				position.translate((int) data.distance.width, 0);
				return Direction.LEFT;
			}
			else {
				position.translate((int) -data.distance.width, 0);
				return Direction.RIGHT;
			}
		}
		else {
			if (correction.y > box2.position.y) {
				position.translate(0, (int) data.distance.height);
				return Direction.UP;
			}
			else {
				position.translate(0, (int) -data.distance.height);
				return Direction.DOWN;
			}
		}
	}
}
