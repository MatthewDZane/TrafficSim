package world;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import entities.Car;
import helpers.Direction;
import helpers.ID;

/**
 * Note: all position values are in centimeters.
 * all speed values are in decimeters for int rounding issues
 * @author matth
 *
 */
public class World {

	private ArrayList<Road> roads = new ArrayList<Road>();
	private ArrayList<ArrayList<Car>> cars = new ArrayList<ArrayList<Car>>();

	public ArrayList<Road> getRoads() { return roads; }
	public ArrayList<ArrayList<Car>> getCars() { return cars; }

	public World() {
		Road road1 = null;
		Road road2 = null;
		Road road3 = null;
		Road road4 = null;
		try {
			road1 = new Road(new Rectangle(-2500, 0, 25000, 2000), 166, 500, 500, 2, 2, this);
			road2 = new Road(new Rectangle(5000, -7000, 2000, 25000), 166, 500, 500, 2, 2, this);
			road3 = new Road(new Rectangle(-2500, 8000, 25000, 2000), 166, 500, 500, 2, 2, this);
			road4 = new Road(new Rectangle(14000, -7000, 2000, 25000), 166, 500, 500, 2, 2, this);
		} catch (Exception e) { e.printStackTrace(); }

		road1.getLanes().get(0).getRoadEnd1().setRdEdObject("Despawner", 0);
		road1.getLanes().get(0).getRoadEnd2().setRdEdObject("Spawner", 0);
		road1.getLanes().get(1).getRoadEnd1().setRdEdObject("Despawner", 1);
		road1.getLanes().get(1).getRoadEnd2().setRdEdObject("Spawner", 1);
		road1.getLanes().get(2).getRoadEnd1().setRdEdObject("Spawner", 2);
		road1.getLanes().get(2).getRoadEnd2().setRdEdObject("Despawner", 2);
		road1.getLanes().get(3).getRoadEnd1().setRdEdObject("Spawner", 3);
		road1.getLanes().get(3).getRoadEnd2().setRdEdObject("Despawner", 3);


		road2.getLanes().get(0).getRoadEnd1().setRdEdObject("Spawner", 0);
		road2.getLanes().get(0).getRoadEnd2().setRdEdObject("Despawner", 0);
		road2.getLanes().get(1).getRoadEnd1().setRdEdObject("Spawner", 1);
		road2.getLanes().get(1).getRoadEnd2().setRdEdObject("Despawner", 1);
		road2.getLanes().get(2).getRoadEnd1().setRdEdObject("Despawner", 2);
		road2.getLanes().get(2).getRoadEnd2().setRdEdObject("Spawner", 2);
		road2.getLanes().get(3).getRoadEnd1().setRdEdObject("Despawner", 3);
		road2.getLanes().get(3).getRoadEnd2().setRdEdObject("Spawner", 3);

		road3.getLanes().get(0).getRoadEnd1().setRdEdObject("Despawner", 0);
		road3.getLanes().get(0).getRoadEnd2().setRdEdObject("Spawner", 0);
		road3.getLanes().get(1).getRoadEnd1().setRdEdObject("Despawner", 1);
		road3.getLanes().get(1).getRoadEnd2().setRdEdObject("Spawner", 1);
		road3.getLanes().get(2).getRoadEnd1().setRdEdObject("Spawner", 2);
		road3.getLanes().get(2).getRoadEnd2().setRdEdObject("Despawner", 2);
		road3.getLanes().get(3).getRoadEnd1().setRdEdObject("Spawner", 3);
		road3.getLanes().get(3).getRoadEnd2().setRdEdObject("Despawner", 3);
		
		road4.getLanes().get(0).getRoadEnd1().setRdEdObject("Spawner", 0);
		road4.getLanes().get(0).getRoadEnd2().setRdEdObject("Despawner", 0);
		road4.getLanes().get(1).getRoadEnd1().setRdEdObject("Spawner", 1);
		road4.getLanes().get(1).getRoadEnd2().setRdEdObject("Despawner", 1);
		road4.getLanes().get(2).getRoadEnd1().setRdEdObject("Despawner", 2);
		road4.getLanes().get(2).getRoadEnd2().setRdEdObject("Spawner", 2);
		road4.getLanes().get(3).getRoadEnd1().setRdEdObject("Despawner", 3);
		road4.getLanes().get(3).getRoadEnd2().setRdEdObject("Spawner", 3);
		
		addRoad(road1);
		addRoad(road2);
		addRoad(road3);
		addRoad(road4);
		
		//road1.addCrosswalk(new Dimension(road1.getPosition().y + 5000, SIDE_WALK_WIDTH), true);
		Timer checkingTimer = new Timer(100, new CheckingTimerListener());	
		checkingTimer.start();
	}

	public void addRoad(Road road) {
		for (Road temp : roads) {
			if (road.getPosition().intersects(temp.getPosition())) {
				Rectangle intersectionPos = road.getPosition().intersection(temp.getPosition());
				int id = ID.getID();
				if (road.isHorizontal()) {
					road.addCarIntersection(
							new Dimension(
									(int)(Math.round(intersectionPos.x - Road.SIDE_WALK_WIDTH)), 
									(int)(Math.round(intersectionPos.width
											+ 2 * Road.SIDE_WALK_WIDTH))), temp, id);
				}
				else {
					road.addCarIntersection(
							new Dimension(
									(int)(Math.round(intersectionPos.y - Road.SIDE_WALK_WIDTH)),
									(int)(Math.round(intersectionPos.height 
											+ 2 * Road.SIDE_WALK_WIDTH))), temp, id);
				}

				if (temp.isHorizontal()) {
					temp.addCarIntersection(
							new Dimension(
									(int)(Math.round(intersectionPos.x - Road.SIDE_WALK_WIDTH)), 
									(int)(Math.round(intersectionPos.width
											+ 2 * Road.SIDE_WALK_WIDTH))), road, id);
				}
				else {
					temp.addCarIntersection(
							new Dimension(
									(int)(Math.round(intersectionPos.y - Road.SIDE_WALK_WIDTH)),
									(int)(Math.round(intersectionPos.height
											+ 2 * Road.SIDE_WALK_WIDTH))), road, id);
				}
			}
		}
		roads.add(road);
	}

	public Rectangle getBounds() {
		Rectangle bounds = roads.get(0).getPosition();

		for (Road temp : roads) {
			bounds = bounds.union(temp.getPosition());
		}

		return bounds;
	}

	public void checkCars() {
		for (Road temp : roads) {
			temp.checkCars();
		}
	}

	public class CheckingTimerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			checkCars();			
		}
	}
}
