package entities;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import helpers.AABB;
import helpers.CarAIProtocol;
import helpers.Collision;
import helpers.Direction;
import helpers.View;
import world.Crosswalk;
import world.Intersection;
import world.Lane;
import world.Road;
import world.CarIntersection;
import world.World;

public class Car extends Entity {
	public static final Dimension DEFAULT_CAR_DIMENSIONS = new Dimension(500, 185);

	public static final int FRONT_VIEW_DISTANCE = 2500;
	public static final int BACK_VIEW_DISTANCE = 1000;
	public static final int SIDE_VIEW_DISTANCE = 1500;

	// x cm per y dm / s
	public static final double FOLLOWING_DISTANCE_TO_SPEED_RATIO = 4.5;
	public static final double STOPPED_DISTANCE = 250;
	public static final double INTERSECTION_STOP_DISTANCE = 75;

	private World world;
	private Road road;
	private Lane lane;
	private Intersection intersection;

	private Direction orientation;

	private TrafficJamTimerListener trafficJamTimerListener = new TrafficJamTimerListener();

	private Timer movementTimer = new Timer(500, new MovementTimerListener(this));
	private Timer accelerationTimer = new Timer(1000 / 200, new AccelerationTimerListener());
	private Timer decelerationTimer = new Timer(1000 / 460, new DecelerationTimerListener());
	private Timer carAITimer = new Timer(1000 / 200, new CarAIListener(this));
	private Timer trafficJamTimer = new Timer(1000, trafficJamTimerListener);


	//dm per sec
	public double speedLimit = 12500 / 9;

	public Road getRoad() { return road; }
	public void setRoad(Road roadIn) { road = roadIn; }

	public Car(Rectangle positionIn, Direction orientationIn, World worldIn, Road roadIn, Lane laneIn) {
		super(positionIn);
		orientation = orientationIn;
		world = worldIn;
		road = roadIn;
		lane = laneIn;
	}

	public void setUpSpeedLimit(double speedLimitIn) { 
		speedLimit = speedLimitIn; 
		movementTimer.setDelay((int) (1000 / speed));
		movementTimer.start();

		carAITimer.start();
	}

	public void delete() {
		movementTimer.stop();
		accelerationTimer.stop();
		decelerationTimer.stop();
		carAITimer.stop();
	}

	public void stopTimer() {
		movementTimer.stop();
	}

	public class MovementTimerListener implements ActionListener {

		private Car car;

		public MovementTimerListener(Car carIn) {
			car = carIn;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (orientation) {
			case UP : position.translate(0, -10);	
			break;
			case DOWN : position.translate(0, 10);	
			break;
			case LEFT : position.translate(-10, 0);	
			break;
			case RIGHT : position.translate(10, 0);	
			break;
			}
			check();
		}

		public void check() {
			for (Intersection temp : road.getIntersections()) {
				if (temp.getPosition().intersects(position)) {
					temp.addEntity(car);
					intersection = temp;
				}
				else {
					temp.getEntities().remove(car);
					intersection = null;
				}
			}
		}
	}

	public class AccelerationTimerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (speed <= speedLimit) {
				if (speed == 0) {
					movementTimer.start();
				}
				speed += 1;

			}
			movementTimer.setDelay((int)(1000 / (speed)));
		}
	}

	public void startAccelerating() {
		accelerationTimer.start();
		decelerationTimer.stop();
	}

	public void stopAccelerating() {
		accelerationTimer.stop();
	}

	public class DecelerationTimerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			speed -= 1;
			if (speed > 0) {
				movementTimer.setDelay((int)(1000 / (speed)));
			}
			else {
				speed = 0;
				decelerationTimer.stop();
				movementTimer.stop();
			}
		}
	}

	public void startDecelerating() {
		decelerationTimer.start();
		accelerationTimer.stop();
	}

	public void stopDecelerating() {
		decelerationTimer.stop();
	}

	public class TrafficJamTimerListener implements ActionListener {
		private int time = 0;

		public int getTime() { return time; }

		@Override
		public void actionPerformed(ActionEvent e) {
			time++;
		}

	}

	public class CarAIListener implements ActionListener {
		private CarAIProtocol protocol = CarAIProtocol.NORMAL;
		private Car car;

		private View view;

		public CarAIListener(Car carIn) {
			car = carIn;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			view = look();
			think();			
		}

		public View look() {
			Rectangle viewPosition = null;

			switch (orientation) {
			case UP :
				viewPosition = new Rectangle(position.x - SIDE_VIEW_DISTANCE,
						position.y - FRONT_VIEW_DISTANCE, 
						position.width + 2 * SIDE_VIEW_DISTANCE, 
						FRONT_VIEW_DISTANCE + position.height + BACK_VIEW_DISTANCE);
				break;
			case DOWN :
				viewPosition = new Rectangle(position.x - SIDE_VIEW_DISTANCE,
						position.y - BACK_VIEW_DISTANCE, 
						position.width + 2 * SIDE_VIEW_DISTANCE, 
						FRONT_VIEW_DISTANCE + position.height + BACK_VIEW_DISTANCE);
				break;
			case LEFT :
				viewPosition = new Rectangle(position.x - FRONT_VIEW_DISTANCE,
						position.y - FRONT_VIEW_DISTANCE, 
						FRONT_VIEW_DISTANCE + position.width + BACK_VIEW_DISTANCE,
						position.height + 2 * SIDE_VIEW_DISTANCE );
				break;
			case RIGHT :
				viewPosition = new Rectangle(position.x - BACK_VIEW_DISTANCE,
						position.y - FRONT_VIEW_DISTANCE, 
						FRONT_VIEW_DISTANCE + position.width + BACK_VIEW_DISTANCE,
						position.height + 2 * SIDE_VIEW_DISTANCE );
				break;
			}



			ArrayList<Intersection> nearbyIntersections = new ArrayList<Intersection>();

			for (Intersection temp : road.getIntersections()) {
				if (temp.getPosition().intersects(viewPosition)) {
					nearbyIntersections.add(temp);
				}
			}


			ArrayList<Car> nearbyCars = new ArrayList<Car>();

			for (Car temp : road.getCars()) {
				if (temp.getPosition().intersects(viewPosition) && 
						!position.equals(temp.getPosition())) {
					nearbyCars.add(temp);
				}
			}

			for (Intersection temp : nearbyIntersections) {
				if (temp instanceof CarIntersection) {
					CarIntersection tempTrafficLightIntersection = (CarIntersection) temp;
					for (Car tempCar : tempTrafficLightIntersection.getOtherRoad().getCars()) {
						if (temp.getPosition().intersects(viewPosition) && 
								!position.equals(temp.getPosition())) {
							nearbyCars.add(tempCar);
						}
					}
				}
			}


			return new View(nearbyCars, nearbyIntersections);
		}

		public void think() {
			if (protocol == CarAIProtocol.NORMAL) {
				//System.out.println("Normal");
				if (frontIsClear()) {
					startAccelerating();
				}
				else if (speed > 0){
					startDecelerating();
				}
			}
			else if (protocol == CarAIProtocol.STOPSIGN) {
				//System.out.println("StopSign");
				if (stopSignProtocol()) {
					startAccelerating();
				}
				else if (speed > 0){
					startDecelerating();
				}
			}
			else if (protocol == CarAIProtocol.INTERSECTION) {
				//System.out.println("Intersection");
				if (intersectionProtocol()) {
					startAccelerating();
				}
				else if (speed > 0){
					startDecelerating();
				}
			}

		}

		public boolean frontIsClear() {
			double significantSpeed = speed;
			if (significantSpeed <= 10) {
				significantSpeed = 0;
			}

			int frontSafeDistance = (int)(FOLLOWING_DISTANCE_TO_SPEED_RATIO * significantSpeed + STOPPED_DISTANCE);

			Rectangle frontSafeSpace = null;


			switch (orientation) {
			case UP :
				frontSafeSpace = new Rectangle(lane.getPosition().x, position.y - frontSafeDistance,	
						lane.getPosition().width, frontSafeDistance);
				break;
			case DOWN :
				frontSafeSpace = new Rectangle(lane.getPosition().x, position.y + position.height,
						lane.getPosition().width, frontSafeDistance);
				break;
			case LEFT :
				frontSafeSpace = new Rectangle(position.x - frontSafeDistance, lane.getPosition().y,
						frontSafeDistance, lane.getPosition().height);
				break;
			case RIGHT :
				frontSafeSpace = new Rectangle(position.x + position.width, lane.getPosition().y,
						frontSafeDistance, lane.getPosition().height);
				break;
			}
			//}
			for (Car temp : view.getNearbyCars()) {
				if (temp.getPosition().intersects(frontSafeSpace)) {
					return false;
				}
			}

			//int intersectionSafeDistance = (int)(FOLLOWING_DISTANCE_TO_SPEED_RATIO * significantSpeed + INTERSECTION_STOP_DISTANCE);



			int stopSignSafeDistance = (int)(FOLLOWING_DISTANCE_TO_SPEED_RATIO * significantSpeed + INTERSECTION_STOP_DISTANCE);

			Rectangle stopSignSafeSpace = null;
			switch (orientation) {
			case UP :
				stopSignSafeSpace = new Rectangle(lane.getPosition().x, position.y - stopSignSafeDistance,	
						lane.getPosition().width, stopSignSafeDistance);
				break;
			case DOWN :
				stopSignSafeSpace = new Rectangle(lane.getPosition().x, position.y + position.height,
						lane.getPosition().width, stopSignSafeDistance);
				break;
			case LEFT :
				stopSignSafeSpace = new Rectangle(position.x - stopSignSafeDistance, lane.getPosition().y,
						stopSignSafeDistance, lane.getPosition().height);
				break;
			case RIGHT :
				stopSignSafeSpace = new Rectangle(position.x + position.width, lane.getPosition().y,
						stopSignSafeDistance, lane.getPosition().height);
				break;
			}

			for (Intersection temp : view.getNearbyIntersections()) {
				if (temp.getPosition().intersects(stopSignSafeSpace)) {
					protocol = CarAIProtocol.STOPSIGN;
					return false;

				}
			}
			return true;
		}

		public boolean stopSignProtocol() {
			double significantSpeed = speed;
			if (significantSpeed <= 10) {
				significantSpeed = 0;
			}
			int frontSafeDistance = (int)(FOLLOWING_DISTANCE_TO_SPEED_RATIO * significantSpeed + STOPPED_DISTANCE);

			Rectangle frontSafeSpace = null;

			switch (orientation) {
			case UP :
				frontSafeSpace = new Rectangle(lane.getPosition().x, position.y - frontSafeDistance,	
						lane.getPosition().width, frontSafeDistance);
				break;
			case DOWN :
				frontSafeSpace = new Rectangle(lane.getPosition().x, position.y + position.height,
						lane.getPosition().width, frontSafeDistance);
				break;
			case LEFT :
				frontSafeSpace = new Rectangle(position.x - frontSafeDistance, lane.getPosition().y,
						frontSafeDistance, lane.getPosition().height);
				break;
			case RIGHT :
				frontSafeSpace = new Rectangle(position.x + position.width, lane.getPosition().y,
						frontSafeDistance, lane.getPosition().height);
				break;
			}

			int stopSignSafeDistance = (int)(FOLLOWING_DISTANCE_TO_SPEED_RATIO * significantSpeed + INTERSECTION_STOP_DISTANCE);

			Rectangle stopSignSafeSpace = null;
			switch (orientation) {
			case UP :
				stopSignSafeSpace = new Rectangle(lane.getPosition().x, position.y - stopSignSafeDistance,	
						lane.getPosition().width, stopSignSafeDistance);
				break;
			case DOWN :
				stopSignSafeSpace = new Rectangle(lane.getPosition().x, position.y + position.height,
						lane.getPosition().width, stopSignSafeDistance);
				break;
			case LEFT :
				stopSignSafeSpace = new Rectangle(position.x - stopSignSafeDistance, lane.getPosition().y,
						(int)(Math.round(stopSignSafeDistance)), lane.getPosition().height);
				break;
			case RIGHT :
				stopSignSafeSpace = new Rectangle((int)(Math.round(position.x + position.width)), lane.getPosition().y,
						(int)(Math.round(stopSignSafeDistance)), lane.getPosition().height);
				break;
			}

			for (Intersection temp : view.getNearbyIntersections()) {
				if (temp.getPosition().intersects(stopSignSafeSpace)) {
					if (speed > 0) {
						return false;
					}
					else {
						protocol = CarAIProtocol.INTERSECTION;
						trafficJamTimer.restart();
						trafficJamTimer.start();
						return true;
					}
				}
			}
			return true;
		}



		public boolean intersectionProtocol() {
			Intersection aheadIntersection = getAheadIntersection();

			if (aheadIntersection == null) {
				protocol = CarAIProtocol.NORMAL;
				return true;
			}
			else {
				double significantSpeed = speed;
				if (significantSpeed <= 10) {
					significantSpeed = 0;
				}

				int frontSafeDistance = (int)(FOLLOWING_DISTANCE_TO_SPEED_RATIO * significantSpeed + STOPPED_DISTANCE);

				Rectangle frontSafeSpace = null;


				switch (orientation) {
				case UP :
					frontSafeSpace = new Rectangle(lane.getPosition().x, 
							(int)(Math.round(position.y - frontSafeDistance 
							- position.height - STOPPED_DISTANCE)),	
							lane.getPosition().width, (int)(Math.round(frontSafeDistance 
							+ position.height + STOPPED_DISTANCE)));
					break;
				case DOWN :
					frontSafeSpace = new Rectangle(lane.getPosition().x, position.y 
							+ position.height, lane.getPosition().width, 
							(int)(Math.round(frontSafeDistance + 
							position.height + STOPPED_DISTANCE)));
					break;
				case LEFT :
					frontSafeSpace = new Rectangle((int)(Math.round(position.x - frontSafeDistance 
							- position.width - STOPPED_DISTANCE)), lane.getPosition().y,
							(int)(Math.round(frontSafeDistance + position.width + STOPPED_DISTANCE)),
							lane.getPosition().height);
					break;
				case RIGHT :
					frontSafeSpace = new Rectangle(position.x + position.width, lane.getPosition().y,
							(int)(Math.round(frontSafeDistance + position.width + STOPPED_DISTANCE)), 
							lane.getPosition().height);
					break;
				}
				try {

					for (Entity temp : road.getCars()) {
						if (temp.getPosition().intersects(frontSafeSpace)) {
							if (trafficJamTimerListener.getTime() > 5) {
								sideAdjust(temp.getPosition());
							}
							else {
								return false;
							}
						}
					}
				} catch(NullPointerException e) {}

				Rectangle intersectionView = null;
				intersectionView = aheadIntersection.getPosition();
				/*switch (orientation) {
				case UP :
					intersectionView = new Rectangle(lane.getPosition().x, 
							aheadIntersection.getPosition().y,
							lane.getPosition().width, 
							position.y - aheadIntersection.getPosition().y );
					break;
				case DOWN :
					intersectionView = new Rectangle(lane.getPosition().x, 
							position.y + position.height, lane.getPosition().width, 
							aheadIntersection.getPosition().y 
							+ aheadIntersection.getPosition().height 
							- position.y - position.height);
					break;
				case LEFT :
					intersectionView = new Rectangle(aheadIntersection.getPosition().x, 
							lane.getPosition().y, position.x - aheadIntersection.getPosition().x, 
							lane.getPosition().height);
					break;
				case RIGHT :
					intersectionView = new Rectangle(position.x + position.width, 
							lane.getPosition().y, aheadIntersection.getPosition().x 
							+ aheadIntersection.getPosition().width - position.x - position.width, 
							lane.getPosition().height);
					break;
				}*/
				if (aheadIntersection instanceof CarIntersection) {
					CarIntersection carIntersection = (CarIntersection) aheadIntersection;
					for (Entity temp : carIntersection.getOtherRoad().getCars()) {
						if (temp.getSpeed() > 0 && temp.getPosition().intersects(intersectionView)) {

							return false;
						}
					}


					switch (orientation) {
					case UP :
						frontSafeSpace = new Rectangle(lane.getPosition().x, 
								aheadIntersection.getPosition().y,
								lane.getPosition().width, 
								position.y - aheadIntersection.getPosition().y );
						break;
					case DOWN :
						frontSafeSpace = new Rectangle(lane.getPosition().x, 
								position.y + position.height, lane.getPosition().width, 
								aheadIntersection.getPosition().y 
								+ aheadIntersection.getPosition().height 
								- position.y - position.height);
						break;
					case LEFT :
						frontSafeSpace = new Rectangle(aheadIntersection.getPosition().x, 
								lane.getPosition().y, position.x - aheadIntersection.getPosition().x, 
								lane.getPosition().height);
						break;
					case RIGHT :
						frontSafeSpace = new Rectangle(position.x + position.width, 
								lane.getPosition().y, aheadIntersection.getPosition().x 
								+ aheadIntersection.getPosition().width - position.x - position.width, 
								lane.getPosition().height);
						break;
					}


					for (Intersection temp : carIntersection.getOtherRoad().getIntersections()) {
						if (temp instanceof CarIntersection && 
								((CarIntersection) temp).getID() == carIntersection.getID()) {
							for (Entity tempCar : temp.getEntities()) {
								if (tempCar.getSpeed() > 0 && tempCar.getPosition().intersects(frontSafeSpace)) {
									return false;
								}
							}
						}
					}
				}




			}
			return true;
		}

		public Intersection getAheadIntersection() {
			Intersection aheadIntersection = null;

			switch (orientation) {
			case UP :
				aheadIntersection = view.getIntersection(
						new Rectangle(position.x, (int)(Math.round(position.y - INTERSECTION_STOP_DISTANCE)),  
								position.width, (int)(Math.round(INTERSECTION_STOP_DISTANCE)) ));
				break;
			case DOWN :
				aheadIntersection = view.getIntersection(
						new Rectangle(position.x, position.y + position.height, position.width, 
								(int)(Math.round(INTERSECTION_STOP_DISTANCE))));
				break;
			case LEFT :
				aheadIntersection = view.getIntersection(
						new Rectangle((int)(Math.round(position.x - INTERSECTION_STOP_DISTANCE)), position.y,  
								(int)(Math.round(INTERSECTION_STOP_DISTANCE)), position.height));
				break;
			case RIGHT :
				aheadIntersection = view.getIntersection(
						new Rectangle(position.x + position.width, position.y,  
								(int)(Math.round(INTERSECTION_STOP_DISTANCE)), position.height));
				break;
			}
			return aheadIntersection;
		}

		public boolean sideAdjust(Rectangle blockingCarPos) {
			

			return false;
		}
	}
}
