package world;

import java.awt.Rectangle;
import java.util.ArrayList;

public class CarIntersection extends Intersection {
	private Road otherRoad;
	private int id;
	
	
	public Road getOtherRoad() { return otherRoad; }
	public int getID() { return id; }
	
	public CarIntersection(Rectangle positionIn, Road otherRoadIn, boolean hasStopSignIn, int idIn) {
		super(positionIn, hasStopSignIn);
		
		otherRoad = otherRoadIn;
		id = idIn;
	}
}
