package helpers;

import java.awt.Rectangle;
import java.util.ArrayList;

import entities.Car;
import world.Intersection;

public class View {
	private ArrayList<Car> nearbyCars;
	private ArrayList<Intersection> nearbyIntersections;
	
	public ArrayList<Car> getNearbyCars() { return nearbyCars; }
	public ArrayList<Intersection> getNearbyIntersections() { return nearbyIntersections; }
	
	public View(ArrayList<Car> nearbyCarsIn, ArrayList<Intersection> nearbyIntersectionsIn) {
		nearbyCars = nearbyCarsIn;
		nearbyIntersections = nearbyIntersectionsIn;
	}
	
	public boolean isNearbyIntersection(Rectangle position) {
		for (Intersection temp : nearbyIntersections) {
			if (temp.getPosition().intersects(position)) {
				return true;
			}
		}
		return false;
	}
	
	public Intersection getIntersection(Rectangle position) {
		for (Intersection temp : nearbyIntersections) {
			if (temp.getPosition().intersects(position)) {
				return temp;
			}
		}
		return null;
	}
}
