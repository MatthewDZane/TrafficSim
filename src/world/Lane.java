package world;

import java.awt.Rectangle;

import entities.Car;
import helpers.Direction;
import world.Road.SpawnerActionListener;

public class Lane {

	public static final int MIN_LANE_WIDTH = 400;

	private Rectangle position;
	private Direction trafficDirection;

	private RoadEnd roadEnd1 = null;
	private RoadEnd roadEnd2 = null;

	public Rectangle getPosition() { return position; }
	public Direction getTrafficDirection() { return trafficDirection; }

	public RoadEnd getRoadEnd1() { return roadEnd1; }
	public RoadEnd getRoadEnd2() { return roadEnd2; }

	public Lane(Rectangle positionIn, Direction trafficDirectionIn, int roadEnd1Length, int roadEnd2Length, SpawnerActionListener listenerIn) {
		position = positionIn;
		trafficDirection = trafficDirectionIn;

		switch (trafficDirection) {
		case UP :
			roadEnd1 = new RoadEnd(new Rectangle(position.x, position.y, position.width, 
					roadEnd1Length), listenerIn);
			roadEnd2 = new RoadEnd(new Rectangle(position.x, position.y + position.height - roadEnd2Length, 
					position.width, roadEnd2Length), listenerIn);
			break;
		case DOWN :
			roadEnd1 = new RoadEnd(new Rectangle(position.x, position.y, position.width, 
					roadEnd1Length), listenerIn);
			roadEnd2 = new RoadEnd(new Rectangle(position.x, position.y + position.height - roadEnd2Length, 
					position.width, roadEnd2Length), listenerIn);
			break;
		case LEFT :
			roadEnd1 = new RoadEnd(new Rectangle(position.x, position.y, roadEnd1Length, 
					position.height), listenerIn);
			roadEnd2 = new RoadEnd(new Rectangle(position.x + position.width - roadEnd2Length, 
					position.y, roadEnd2Length, position.height), listenerIn);
			break;
		case RIGHT :
			roadEnd1 = new RoadEnd(new Rectangle(position.x, position.y, roadEnd1Length, 
					position.height), listenerIn);
			roadEnd2 = new RoadEnd(new Rectangle(position.x + position.width - roadEnd2Length, 
					position.y, roadEnd2Length, position.height), listenerIn);
			break;
		}
	}
	
	//returns true if car intersects a despawner
	public boolean checkCar(Car car) {
		if (getRoadEnd1().getRdEdObject() instanceof Despawner) {
			if (car.getPosition().intersects(getRoadEnd1().getPosition())) {
				car.delete();
				return true;
			}
		}
		else if (getRoadEnd2().getRdEdObject() instanceof Despawner) {
			if (car.getPosition().intersects(getRoadEnd2().getPosition())) {
				car.delete();
				return true;
			}
		}
		return false;
	}
}
