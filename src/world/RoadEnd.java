package world;

import java.awt.Point;
import java.awt.Rectangle;

import world.Road.SpawnerActionListener;

/**
 * Road Ends constrained within a two-dimensional space described 
 * by a one-dimensional space within two specified points.
 * @author matth
 *
 */
public class RoadEnd {

	private Rectangle position;

	private RoadEndObject rdEdObject = new RoadEndObject();

	private SpawnerActionListener listener;

	public Rectangle getPosition() { return position; }

	public RoadEndObject getRdEdObject() { return rdEdObject; }
	public void setRdEdObject(RoadEndObject rdEdObjectIn) { rdEdObject = rdEdObjectIn; }

	public RoadEnd(Rectangle positionIn, SpawnerActionListener listenerIn ) {
		position = positionIn;

		listener = listenerIn;
	}

	public void setRdEdObject(String rdEdObjectIn, int lane) {
		if (rdEdObjectIn.equals("Spawner")) {
			rdEdObject = new Spawner(listener, lane);
		}
		else if (rdEdObjectIn.equals("Despawner")) {
			rdEdObject = new Despawner();
		}
	}

}
