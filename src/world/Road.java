package world;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;

import entities.Car;
import helpers.Direction;

public class Road {
	private World world;

	private Rectangle position;
	
	public static final double SIDE_WALK_WIDTH = 150;

	private ArrayList<Lane> lanes = new ArrayList<Lane>();
	private ArrayList<Car> cars = new ArrayList<Car>();
	private ArrayList<Intersection> intersections = new ArrayList<Intersection>();

	//speed dm / sec
	private double speedLimit;

	public Rectangle getPosition() { return position; }

	public ArrayList<Lane> getLanes() { return lanes; }
	public ArrayList<Car> getCars() { return cars; }
	public ArrayList<Intersection> getIntersections() { return intersections; }

	/**
	 * Precondition: total number of lanes allow lane width to be greater than MIN_LANE_WIDTH
	 * @param positionIn
	 * @param speedLimitIn
	 * @param isHorizontalIn
	 * @param rdEd1Pos
	 * @param rdEd2Pos
	 * @param numPositiveLanes - number of lanes with direction of traffic in the positive x or y 
	 * direction depending on the road orientation.
	 * @param numNegativeLanes - number of lanes with direction of traffic in the negative x or y 
	 * direction depending on the road orientation.
	 * @param world
	 */
	public Road(Rectangle positionIn, double speedLimitIn, int roadEnd1Length, int roadEnd2Length, 
			int numPositiveLanes, int numNegativeLanes, World world) throws Exception {
		position = positionIn;
		speedLimit = speedLimitIn;

		int width;
		if (isHorizontal()) {
			width = position.height;

			limitNumLanes(width, numPositiveLanes + numNegativeLanes);

			for (int i = 0; i < numNegativeLanes; i++) {
				lanes.add(new Lane(new Rectangle(position.x, (int)(position.y + width * i / (numPositiveLanes + numNegativeLanes)),
						position.width, (int)(width / (numPositiveLanes + numNegativeLanes))), 
						Direction.LEFT, roadEnd1Length, roadEnd2Length, new SpawnerActionListener(this)));
			}
			for (int i = numNegativeLanes; i < numNegativeLanes + numPositiveLanes; i++) {
				lanes.add(new Lane(new Rectangle(position.x, (int)(position.y + width * i / (numPositiveLanes + numNegativeLanes)),
						position.width, (int)(width / (numPositiveLanes + numNegativeLanes))), 
						Direction.RIGHT, roadEnd1Length, roadEnd2Length, new SpawnerActionListener(this)));
			}
		}
		else {
			width = position.width;

			limitNumLanes(width, numPositiveLanes + numNegativeLanes);

			for (int i = 0; i < numPositiveLanes; i++) {
				lanes.add(new Lane(new Rectangle((int)(position.x + width * i / (numPositiveLanes + numNegativeLanes)), 
						position.y, (int)(width / (numPositiveLanes + numNegativeLanes)), 
						position.height), Direction.DOWN, roadEnd1Length, roadEnd2Length, new SpawnerActionListener(this)));
			}
			for (int i = numPositiveLanes; i < numNegativeLanes + numPositiveLanes; i++) {
				lanes.add(new Lane(new Rectangle((int)(position.x + width * i / (numPositiveLanes + numNegativeLanes)), 
						position.y, (int)(width / (numPositiveLanes + numNegativeLanes)), 
						position.height), Direction.UP, roadEnd1Length, roadEnd2Length, new SpawnerActionListener(this)));
			}
		}
		
		world.getCars().add(cars);
	}

	public boolean isHorizontal() {
		return position.width > position.height;
	}
	
	public double getWidth() {
		if (isHorizontal()) {
			return position.height;
		}
		else {
			return position.width;
		}
	}

	public double getLaneWidth() {
		return getWidth() / lanes.size();
	}

	private void limitNumLanes(int roadWidth, int numLanes) throws Exception {
		int maxLanes = (int) Math.ceil(roadWidth / Lane.MIN_LANE_WIDTH);
		if (maxLanes < numLanes) {
			throw new Exception("Too many lanes");
		}
	}

	//width = undefined coordinate from road. height = width of crosswalk
	public void addCrosswalk(Dimension position, boolean hasStopSign) {
		Crosswalk newCrosswalk;
		if (isHorizontal()) {
			newCrosswalk = new Crosswalk(new Rectangle(position.width, 
					getPosition().y, position.height, 
					getPosition().height), hasStopSign);
		}
		else {
			newCrosswalk = new Crosswalk(new Rectangle(getPosition().x, 
					position.width, getPosition().width, 
					position.height), hasStopSign);
		}
		intersections.add(newCrosswalk);
	}
	
	public void addCarIntersection(Dimension position, Road otherRoad, int idIn) {
		CarIntersection newCarInterSection;
		if (isHorizontal()) {
			newCarInterSection = new CarIntersection(new Rectangle(position.width, 
					getPosition().y, position.height, 
					getPosition().height), otherRoad, true, idIn);
		}
		else {
			newCarInterSection = new CarIntersection(new Rectangle(getPosition().x, 
					position.width, getPosition().width, 
					position.height), otherRoad, true, idIn);
		}
		intersections.add(newCarInterSection);
	}

	public void addTrafficLightIntersection(Dimension position, Road otherRoad, int idIn) {
		CarIntersection newTrafficLightInterSection;
		if (isHorizontal()) {
			newTrafficLightInterSection = new CarIntersection(new Rectangle(position.width, 
					getPosition().y, position.height, 
					getPosition().height), otherRoad, true, idIn);
		}
		else {
			newTrafficLightInterSection = new CarIntersection(new Rectangle(getPosition().x, 
					position.width, getPosition().width, 
					position.height), otherRoad, true, idIn);
		}
		intersections.add(newTrafficLightInterSection);
	}
	
	public void checkCars() {
		for (int i = 0; i < cars.size(); i++) {
			if (checkLanes(cars.get(i))) {
				cars.remove(i);
				i--;
			}
		}
	}
	
	public boolean checkLanes(Car car) {
		for (Lane temp : lanes) {
			if (temp.checkCar(car)) {
				return true;
			}
		}
		return false;
	}

	public class SpawnerActionListener {
		private Road road;
		private int numLanes = lanes.size();

		public int getNumLanes() { return numLanes;	}

		public SpawnerActionListener(Road roadIn) {
			road = roadIn;
		}

		public void spawnCar(int laneNum) {
			Lane lane = lanes.get(laneNum);
			Rectangle lanePos = lane.getPosition();
			Car newCar;
			switch (lane.getTrafficDirection()) {
			case UP : 
				newCar = new Car(new Rectangle(lanePos.x + (lanePos.width - Car.DEFAULT_CAR_DIMENSIONS.height) / 2,
						lanePos.y + lanePos.height - Car.DEFAULT_CAR_DIMENSIONS.width, Car.DEFAULT_CAR_DIMENSIONS.height, 
						Car.DEFAULT_CAR_DIMENSIONS.width), Direction.UP, world, road, lane);
				break;
			case DOWN : 
				newCar = new Car(new Rectangle(lanePos.x + (lanePos.width - Car.DEFAULT_CAR_DIMENSIONS.height) / 2,
						lanePos.y,Car.DEFAULT_CAR_DIMENSIONS.height, Car.DEFAULT_CAR_DIMENSIONS.width),
						Direction.DOWN, world, road, lane);
				break;
			case LEFT :
				newCar = new Car(new Rectangle(lanePos.x + lanePos.width - Car.DEFAULT_CAR_DIMENSIONS.width,
						lanePos.y + (lanePos.height - Car.DEFAULT_CAR_DIMENSIONS.height) / 2,
						Car.DEFAULT_CAR_DIMENSIONS.width, Car.DEFAULT_CAR_DIMENSIONS.height),
						Direction.LEFT, world, road, lane); 
				break;
			case RIGHT : 
				newCar = new Car(new Rectangle(lanePos.x, 
						lanePos.y + (lanePos.height - Car.DEFAULT_CAR_DIMENSIONS.height) / 2,
						Car.DEFAULT_CAR_DIMENSIONS.width, Car.DEFAULT_CAR_DIMENSIONS.height),
						Direction.RIGHT, world, road, lane);

				break;
			default : newCar = new Car(null, null, null, null, null);
			break;
			}
			if (isClearOfCars(newCar.getPosition())) {
				cars.add(newCar);
				newCar.setUpSpeedLimit(speedLimit);
			}
		}
	}

	public boolean isClearOfCars(Rectangle area) {
		for (Car temp : cars) {
			if (temp.getPosition().intersects(area)) {
				return false;
			}
		}
		return true;
	}
}
