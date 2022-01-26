package display;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

import helpers.Direction;
import entities.Car;
import world.CarIntersection;
import world.Crosswalk;
import world.Despawner;
import world.Intersection;
import world.Lane;
import world.Road;
import world.RoadEnd;
import world.Spawner;
import world.World;

/**
 * JPanel from which graphics of the World will be drawn 
 * directly on. Uses keyboard and mouse for input.
 * @author Matthew Zane
 * @version 1.1
 * @since 2017-09-16
 */
public class WorldPanel extends JPanel {

	/**
	 * Percentage of the height and width of the parent component
	 * Panel height and width equal parent component height and width
	 * times PCT_OF_PARENT divided by 100, respectively.
	 */
	public static final int PCT_OF_PARENT = 90;

	//Units in Cm
	public static final double BROKEN_LINE_SEGMENT_LENGTH = 300;
	public static final double ROAD_LINE_WIDTH = 20;

	public BufferedImage stopSignTexture = null;

	private World world;
	private Camera camera;

	/**
	 * The current directions that the camera is moving in
	 */
	private ArrayList<Direction> directions = new ArrayList<Direction>();

	private CameraMovementHandler cameraMovementHandler = new CameraMovementHandler();
	private Timer scrollTimer;
	//Units in dm / sec
	private int scrollSpeed = 300;

	//Don't know what the units are
	private int zoomSpeed = 2;

	/**
	 * Initializes a JPanel with a line border and initializes textures
	 * @param worldIn - World object the panel will be displaying from
	 */
	public WorldPanel(World worldIn) {
		super();

		//add listeners
		addMouseWheelListener(new MouseWheelActionListener());
		addKeyListener(new KeyHandler());
		addMouseListener(new MouseHandler());

		world = worldIn;

		setBorder(BorderFactory.createLineBorder(Color.BLACK));

		scrollTimer = new Timer(1000 / scrollSpeed, cameraMovementHandler);


		//attempt to load textures
		try {
			stopSignTexture = ImageIO.read(new File("./pictures/redOctagon.png"));
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * Used to create the camera after the WorldPanel has been created
	 */
	public void createCamera() {
		camera = new Camera(world, this);
		scrollTimer.start();
	}

	/**
	 * Paints graphics of world
	 */
	public void paint(Graphics g) { 
		super.paint(g);

		g.setColor(Color.BLACK);

		paintRoads(g);
		paintCars(g);
	}

	/**
	 * Paints all of the roads
	 * @param g - Graphics of panel
	 */
	public void paintRoads(Graphics g) {
		g.setColor(Color.BLACK);

		for (Road temp : world.getRoads()) {
			paintRoad(g, temp);
		}
	}

	/**
	 * Paints specified road
	 * @param g - Graphics of panel
	 * @param road - specified road
	 */
	public void paintRoad(Graphics g, Road road) {
		Rectangle scaledRoadPos = scalePosition(road.getPosition());
		g.drawRect(scaledRoadPos.x, scaledRoadPos.y, 
				scaledRoadPos.width, scaledRoadPos.height);

		paintLanes(g, road);
		paintRoadEnds(g, road);
		paintIntersections(g, road);
	}

	/**
	 * Paints all lanes of specified road
	 * @param g - Graphics of panel
	 * @param road - specified road
	 */
	public void paintLanes(Graphics g, Road road) {
		for (int i = 1; i < road.getLanes().size(); i++) {
			Lane lane = road.getLanes().get(i);

			paintLane(g, road, lane, i);
		}
	}

	/**
	 * Paints the specified lane
	 * @param g - Graphics of panel
	 * @param road - specified road 
	 * @param lane - specified lane
	 * @param laneNum - lane number
	 */
	public void paintLane(Graphics g, Road road, Lane lane, int laneNum) {
		Rectangle lanePosition = lane.getPosition();
		//Rectangle scaledLanePosition = scalePosition(lanePosition);

		g.setColor(new Color(255, 191, 0));

		if (road.getLanes().get(laneNum - 1).getTrafficDirection() 
				== lane.getTrafficDirection()) {
			paintLaneLine(g, lane, lanePosition);
		} 
		else {
			paintCenterLines(g, lane, lanePosition);
		}
	}

	/**
	 * Paint dotted lines in between lanes of the same direction of
	 * traffic flow
	 * @param g - Graphics of panel
	 * @param lane - specified lane
	 * @param lanePosition - position of specified lane
	 */
	public void paintLaneLine(Graphics g, Lane lane, Rectangle lanePosition) {
		switch (lane.getTrafficDirection()) {
		case UP :
			paintVerticalLaneLine(g, lane, lanePosition);
			break;
		case DOWN :
			paintVerticalLaneLine(g, lane, lanePosition);
			break;
		case LEFT :
			paintHorizontalLaneLine(g, lane, lanePosition);
			break;
		case RIGHT :
			paintHorizontalLaneLine(g, lane, lanePosition);
			break;
		}
	}

	/**
	 * Paints a vertical center line for dividing up and down traffic
	 * @param g - Graphics of panel
	 * @param lane - specified lane
	 * @param lanePosition - position of specified lane
	 */
	public void paintVerticalLaneLine(Graphics g, Lane lane, Rectangle lanePosition) {
		for (int j = lane.getRoadEnd1().getPosition().y
				+ lane.getRoadEnd1().getPosition().height; 
				j < lane.getRoadEnd2().getPosition().y 
				- BROKEN_LINE_SEGMENT_LENGTH; 
				j += 4 * BROKEN_LINE_SEGMENT_LENGTH) {
			paintVerticalLineSegment(g, lanePosition, j);
		}
	}

	/**
	 * Paints the individual line segment for the vertical broken line
	 * @param g - Graphics of panel
	 * @param lane - specified lane
	 * @param lanePosition - position of specified lane
	 */
	public void paintVerticalLineSegment(Graphics g, Rectangle lanePosition, int segmentNum) {
		Rectangle brokenLineSegmentPos = 
				new Rectangle((int)(Math.round(lanePosition.x - 
						ROAD_LINE_WIDTH / 2)), segmentNum,
						(int)(Math.round(ROAD_LINE_WIDTH)), 
						(int)(Math.round(BROKEN_LINE_SEGMENT_LENGTH)));
		Rectangle scaledBrokenLineSegmentPos = scalePosition(brokenLineSegmentPos);
		g.fillRect(scaledBrokenLineSegmentPos.x, 
				scaledBrokenLineSegmentPos.y,
				scaledBrokenLineSegmentPos.width, 
				scaledBrokenLineSegmentPos.height);

	}

	/**
	 * Paints a horizontal center line for dividing up and down traffic
	 * @param g - Graphics of panel
	 * @param lane - specified lane
	 * @param lanePosition - position of specified lane
	 */
	public void paintHorizontalLaneLine(Graphics g, Lane lane, Rectangle lanePosition) {
		for (int j = lane.getRoadEnd1().getPosition().x + lane.getRoadEnd1().getPosition().width; 
				j < lane.getRoadEnd2().getPosition().x - BROKEN_LINE_SEGMENT_LENGTH; 
				j += 4 * BROKEN_LINE_SEGMENT_LENGTH) {
			paintHorizontalLineSegment(g, lanePosition, j);
		}
	}

	/**
	 * Paints the individual line segment for the horizontal broken line
	 * @param g - Graphics of panel
	 * @param lane - specified lane
	 * @param lanePosition - position of specified lane
	 */
	public void paintHorizontalLineSegment(Graphics g, Rectangle lanePosition, int segmentNum) {
		Rectangle brokenLineSegmentPos = 
				new Rectangle(segmentNum, (int)(Math.round(lanePosition.y - 
						ROAD_LINE_WIDTH / 2)),
						(int)(Math.round(BROKEN_LINE_SEGMENT_LENGTH)), 
						(int)(Math.round(ROAD_LINE_WIDTH)));
		Rectangle scaledBrokenLineSegmentPos = 
				scalePosition(brokenLineSegmentPos);
		g.fillRect(scaledBrokenLineSegmentPos.x, 
				scaledBrokenLineSegmentPos.y,
				scaledBrokenLineSegmentPos.width, 
				scaledBrokenLineSegmentPos.height);
	}



	/**
	 * Paints line dividing lanes with different directions of traffic
	 * @param g - Graphics of panel
	 * @param lane - specified lane
	 * @param lanePosition - position of specified lane
	 */
	public void paintCenterLines(Graphics g, Lane lane, Rectangle lanePosition) {
		switch (lane.getTrafficDirection()) {
		case UP :
			paintVerticalCenterLine(g, lane, lanePosition);
			break;
		case DOWN :
			paintVerticalCenterLine(g, lane, lanePosition);
			break;
		case LEFT :
			paintHorizontalCenterLine(g, lane, lanePosition);
			break;
		case RIGHT :
			paintHorizontalCenterLine(g, lane, lanePosition);
			break;
		}
	}

	/**
	 * Paints a vertical center line for dividing up and down traffic
	 * @param g - Graphics of panel
	 * @param lane - specified lane
	 * @param lanePosition - position of specified lane
	 */
	public void paintVerticalCenterLine(Graphics g, Lane lane, Rectangle 
			lanePosition) {
		Rectangle solidUpLinePos = 
				new Rectangle((int)(Math.round(lanePosition.x
				- ROAD_LINE_WIDTH / 2)),
				lane.getRoadEnd1().getPosition().y + 
				lane.getRoadEnd1().getPosition().height, 
				(int)(Math.round(ROAD_LINE_WIDTH)), 
				lane.getRoadEnd2().getPosition().y
				- lane.getRoadEnd1().getPosition().y 
				- lane.getRoadEnd1().getPosition().height);
		Rectangle scaledUpSolidLinePos = scalePosition(solidUpLinePos);
		g.fillRect(scaledUpSolidLinePos.x, scaledUpSolidLinePos.y,
				scaledUpSolidLinePos.width, scaledUpSolidLinePos.height);
	}

	/**
	 * Paints a horizontal center line for dividing left and right traffic
	 * @param g - Graphics of panel
	 * @param lane - specified lane
	 * @param lanePosition - position of specified lane
	 */
	public void paintHorizontalCenterLine(Graphics g, Lane lane, 
			Rectangle lanePosition) {
		Rectangle solidLeftLinePos = 
				new Rectangle(lane.getRoadEnd1().getPosition().x
						+ lane.getRoadEnd1().getPosition().width, 
						(int)(Math.round(lanePosition.y - ROAD_LINE_WIDTH / 2)), 
						lane.getRoadEnd2().getPosition().x 
						- lane.getRoadEnd1().getPosition().x
						- lane.getRoadEnd1().getPosition().width, 
						(int)(Math.round(ROAD_LINE_WIDTH)));
		Rectangle scaledLeftSolidLinePos = scalePosition(solidLeftLinePos);
		g.fillRect(scaledLeftSolidLinePos.x, scaledLeftSolidLinePos.y,
				scaledLeftSolidLinePos.width, scaledLeftSolidLinePos.height);
	}

	/**
	 * Paints all the Road Ends
	 * @param g - Graphics of panel
	 * @param road - specified road
	 */
	public void paintRoadEnds(Graphics g, Road road) {
		for (Lane tempLane : road.getLanes()) {
			RoadEnd roadEnd1 = tempLane.getRoadEnd1();
			RoadEnd roadEnd2 = tempLane.getRoadEnd2();

			Rectangle scaledPos1 = scalePosition(roadEnd1.getPosition());
			Rectangle scaledPos2 = scalePosition(roadEnd2.getPosition());

			if (roadEnd1.getRdEdObject() instanceof Spawner) {
				g.setColor(Color.GREEN);
				g.fillRect(scaledPos1.x + 1, scaledPos1.y + 1, 
						scaledPos1.width - 1, scaledPos1.height - 1);
			}
			else if (roadEnd1.getRdEdObject() instanceof Despawner) {
				g.setColor(Color.RED);
				g.fillRect(scaledPos1.x + 1, scaledPos1.y + 1, 
						scaledPos1.width - 1, scaledPos1.height - 1);
			}

			g.setColor(Color.BLACK);
			g.drawRect(scaledPos1.x, scaledPos1.y, scaledPos1.width, 
					scaledPos1.height);


			if (roadEnd2.getRdEdObject() instanceof Spawner) {
				g.setColor(Color.GREEN);
				g.fillRect(scaledPos2.x + 1, scaledPos2.y + 1, 
						scaledPos2.width - 1, scaledPos2.height - 1);
			}
			else if (roadEnd2.getRdEdObject() instanceof Despawner) {
				g.setColor(Color.RED);
				g.fillRect(scaledPos2.x + 1, scaledPos2.y + 1, 
						scaledPos2.width - 1, scaledPos2.height - 1);
			}

			g.setColor(Color.BLACK);
			g.drawRect(scaledPos2.x, scaledPos2.y, scaledPos2.width, 
					scaledPos2.height);
		}
	}


	/**
	 * Paints all of intersection for the specified road
	 * @param g - Graphics of panel
	 * @param road - specified road
	 */
	public void paintIntersections(Graphics g, Road road) {
		ArrayList<Intersection> intersections = road.getIntersections();

		for (Intersection tempIntersection : intersections) {
			paintIntersection(g, road, tempIntersection);
		}
	}

	

	/**
	 * Paints specified intersection
	 * @param g - Graphics of panel
	 * @param road - road of intersection
	 * @param intersection - specified intersection
	 */
	public void paintIntersection(Graphics g, Road road, Intersection intersection) {
		Rectangle intersectionPos = intersection.getPosition();
		Rectangle scaledIntersectionPos = scalePosition(intersectionPos);

		g.setColor(getBackground());
		g.fillRect(scaledIntersectionPos.x, scaledIntersectionPos.y, 
				scaledIntersectionPos.width, scaledIntersectionPos.height);

		g.setColor(Color.BLACK);
		g.drawRect(scaledIntersectionPos.x, scaledIntersectionPos.y, 
				scaledIntersectionPos.width, scaledIntersectionPos.height);

		if (intersection.hasStopSign()) {
			paintStopSign(g, road, intersectionPos);
		}
		if (intersection instanceof CarIntersection) {
			paintCarIntersection(g, intersection);
		}
		if (intersection instanceof Crosswalk) {
			paintCrosswalk(g, road, intersection, scaledIntersectionPos);
		}
	}

	/**
	 * Paints stop sign for intersections that have one
	 * @param g - Graphics of panel
	 * @param road - road of intersection
	 * @param intersectionPos - position of intersection
	 */
	public void paintStopSign(Graphics g, Road road, Rectangle intersectionPos) {
		g.setColor(Color.RED);

		Rectangle stopSignPos1 = null;
		Rectangle scaledStopSignPos1 = null;

		//direction of first lane in a road
		switch (road.getLanes().get(0).getTrafficDirection()) {
		case UP :
			stopSignPos1 = 
			new Rectangle((int)(Math.round(intersectionPos.x - road.getLaneWidth() / 2 - road.getLaneWidth() / 16)), 
					(int)(Math.round(intersectionPos.y  + intersectionPos.height - (road.getLaneWidth() / 4))), 
					(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
			break;
		case DOWN :
			stopSignPos1 = 
			new Rectangle((int)(Math.round(intersectionPos.x - road.getLaneWidth() / 2 - road.getLaneWidth() / 16)), 
					(int)(Math.round(intersectionPos.y - (road.getLaneWidth() / 4))), 
					(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
			break;
		case LEFT :
			stopSignPos1 = 
			new Rectangle((int)(Math.round(intersectionPos.x + intersectionPos.width - (road.getLaneWidth() / 4))), 
					(int)(Math.round(intersectionPos.y - road.getLaneWidth() / 2 - road.getLaneWidth() / 16)), 
					(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
			break;
		case RIGHT :
			stopSignPos1 = 
			new Rectangle((int)(Math.round(intersectionPos.x - (road.getLaneWidth() / 4))), 
					(int)(Math.round(intersectionPos.y - road.getLaneWidth() / 2 - road.getLaneWidth() / 16)), 
					(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
			break;
		}
		scaledStopSignPos1 = scalePosition(stopSignPos1);

		Rectangle stopSignPos2 = null;
		Rectangle scaledStopSignPos2 = null;

		//direction of last lane in road
		switch (road.getLanes().get(road.getLanes().size() - 1).getTrafficDirection()) {
		case UP :
			stopSignPos2 = 
			new Rectangle((int)(Math.round(intersectionPos.x + intersectionPos.width + (road.getLaneWidth() / 16))), 
					(int)(Math.round(intersectionPos.y + intersectionPos.height - (road.getLaneWidth() / 4))), 
					(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
			break;
		case DOWN :
			stopSignPos2 = 
			new Rectangle((int)(Math.round(intersectionPos.x + intersectionPos.width + (road.getLaneWidth() / 16))), 
					(int)(Math.round(intersectionPos.y - (road.getLaneWidth() / 4))), 
					(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
			break;
		case LEFT :
			stopSignPos2 = 
			new Rectangle((int)(Math.round(intersectionPos.x + intersectionPos.width - (road.getLaneWidth() / 4))), 
					(int)(Math.round(intersectionPos.y + intersectionPos.height + road.getLaneWidth() / 16)), 
					(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
			break;
		case RIGHT :
			stopSignPos2 = 
			new Rectangle((int)(Math.round(intersectionPos.x - (road.getLaneWidth() / 4))), 
					(int)(Math.round(intersectionPos.y + intersectionPos.height + road.getLaneWidth() / 16)), 
					(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
			break;
		}
		scaledStopSignPos2 = scalePosition(stopSignPos2);

		g.drawImage(stopSignTexture, scaledStopSignPos1.x, scaledStopSignPos1.y, 
				scaledStopSignPos1.width, scaledStopSignPos1.height, null);				
		g.drawImage(stopSignTexture, scaledStopSignPos2.x, scaledStopSignPos2.y, 
				scaledStopSignPos2.width, scaledStopSignPos2.height, null);
	}

	/**
	 * Paints an intersection that is for cars
	 * @param g - Graphics of panel
	 * @param intersection - specified car intersection
	 */
	public void paintCarIntersection(Graphics g, Intersection intersection) {
		CarIntersection carIntersection = (CarIntersection) intersection;
		ArrayList<Intersection> otherCarIntersection = carIntersection.getOtherRoad().getIntersections();

		for (Intersection otherTempIntersection : otherCarIntersection) {
			Rectangle otherIntersectionPos = otherTempIntersection.getPosition();
			Rectangle otherScaledIntersectionPos = scalePosition(otherIntersectionPos);

			g.setColor(Color.BLACK);
			g.drawRect(otherScaledIntersectionPos.x, otherScaledIntersectionPos.y, 
					otherScaledIntersectionPos.width, otherScaledIntersectionPos.height);
		}
	}
	
	/**
	 * Paints specified crosswalk
	 * @param g - Graphics of panel
	 * @param road - Road of intersection
	 * @param intersection - specified intersection
	 * @param intersectionPos - position of intersection
	 */
	public void paintCrosswalk(Graphics g, Road road, 
			Intersection intersection, Rectangle intersectionPos) {
		Crosswalk crosswalk = (Crosswalk) intersection;

		if (crosswalk.hasStopSign()) {
			g.setColor(Color.RED);

			Rectangle stopSignPos1 = null;
			Rectangle scaledStopSignPos1 = null;
			//direction of first lane in a road
			switch (road.getLanes().get(0).getTrafficDirection()) {
			case UP :
				stopSignPos1 = 
				new Rectangle((int)(Math.round(intersectionPos.x - road.getLaneWidth() / 2 - road.getLaneWidth() / 16)), 
						(int)(Math.round(intersectionPos.y  + intersectionPos.height - (road.getLaneWidth() / 4))), 
						(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
				break;
			case DOWN :
				stopSignPos1 = 
				new Rectangle((int)(Math.round(intersectionPos.x - road.getLaneWidth() / 2 - road.getLaneWidth() / 16)), 
						(int)(Math.round(intersectionPos.y - (road.getLaneWidth() / 4))), 
						(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
				break;
			case LEFT :
				stopSignPos1 = 
				new Rectangle((int)(Math.round(intersectionPos.x + intersectionPos.width - (road.getLaneWidth() / 4))), 
						(int)(Math.round(intersectionPos.y - road.getLaneWidth() / 2 - road.getLaneWidth() / 16)), 
						(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
				break;
			case RIGHT :
				stopSignPos1 = 
				new Rectangle((int)(Math.round(intersectionPos.x - (road.getLaneWidth() / 4))), 
						(int)(Math.round(intersectionPos.y - road.getLaneWidth() / 2 - road.getLaneWidth() / 16)), 
						(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
				break;
			}
			scaledStopSignPos1 = scalePosition(stopSignPos1);

			Rectangle stopSignPos2 = null;
			Rectangle scaledStopSignPos2 = null;

			//direction of last lane in road
			switch (road.getLanes().get(road.getLanes().size() - 1).getTrafficDirection()) {
			case UP :
				stopSignPos2 = 
				new Rectangle((int)(Math.round(intersectionPos.x + intersectionPos.width + (road.getLaneWidth() / 16))), 
						(int)(Math.round(intersectionPos.y + intersectionPos.height - (road.getLaneWidth() / 4))), 
						(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
				break;
			case DOWN :
				stopSignPos2 = 
				new Rectangle((int)(Math.round(intersectionPos.x + intersectionPos.width + (road.getLaneWidth() / 16))), 
						(int)(Math.round(intersectionPos.y - (road.getLaneWidth() / 4))), 
						(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
				break;
			case LEFT :
				stopSignPos2 = 
				new Rectangle((int)(Math.round(intersectionPos.x + intersectionPos.width - (road.getLaneWidth() / 4))), 
						(int)(Math.round(intersectionPos.y + intersectionPos.height + road.getLaneWidth() / 16)), 
						(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
				break;
			case RIGHT :
				stopSignPos2 = 
				new Rectangle((int)(Math.round(intersectionPos.x - (road.getLaneWidth() / 4))), 
						(int)(Math.round(intersectionPos.y + intersectionPos.height + road.getLaneWidth() / 16)), 
						(int)(Math.round(road.getLaneWidth() / 2)), (int)(Math.round(road.getLaneWidth() / 2)));
				break;
			}
			scaledStopSignPos2 = scalePosition(stopSignPos2);

			g.drawImage(stopSignTexture, scaledStopSignPos1.x, scaledStopSignPos1.y, 
					scaledStopSignPos1.width, scaledStopSignPos1.height, null);				
			g.drawImage(stopSignTexture, scaledStopSignPos2.x, scaledStopSignPos2.y, 
					scaledStopSignPos2.width, scaledStopSignPos2.height, null);
		}
	}

	/**
	 * Paints all of the cars
	 * @param g - Graphics of panel
	 */
	public void paintCars(Graphics g) {
		for (ArrayList<Car> tempCars : world.getCars()) {
			for (Car tempCar : tempCars) {
				paintCar(g, tempCar);
			}
		}
	}

	/**
	 * Paints specified car
	 * @param g - Graphics of panel
	 * @param car - specified car
	 */
	public void paintCar(Graphics g, Car car) {
		Rectangle scaledPosition = scalePosition(car.getPosition());
		g.setColor(Color.BLACK);
		g.fillRect(scaledPosition.x, scaledPosition.y, scaledPosition.width, scaledPosition.height);
	}



	/**
	 * Returns a scaled position so that it will fit in the camera view
	 * @param position - position to be scaled
	 * @return - scaled position
	 */
	public Rectangle scalePosition(Rectangle position) {
		return new Rectangle((int)(Math.round((position.x * camera.getScale() + camera.getXalign()))), 
				(int)(Math.round((position.y * camera.getScale() + camera.getYalign()))),
				(int)(Math.round((position.width * camera.getScale()))), 
				(int)(Math.round((position.height * camera.getScale()))));
	}

	/**
	 * Zooms the camera in or out depending scroll direction
	 * @author Matthew Zane
	 * @version 1.1
	 * @since 2017-09-15
	 */
	private class MouseWheelActionListener implements MouseWheelListener {

		/**
		 * Uses mouseWheelEvent to either zoom in or out
		 * @Override
		 */
		public void mouseWheelMoved(MouseWheelEvent e) {
			int notches = e.getWheelRotation();
			if (notches < 0) {
				camera.zoom(-zoomSpeed);
			}
			else {
				camera.zoom(zoomSpeed);
			}
		}
	}

	/**
	 * Moves the camera either up, down, left, or right
	 * @author Matthew Zane
	 * @version 1.1
	 * @since 2017-09-15
	 */
	private class KeyHandler implements KeyListener {

		/**
		 * Adds new direction to direction list if not already
		 * in it using the arrow keys or center the camera, if 
		 * the space bar is pressed
		 * @Override
		 */
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();
			switch (keyCode) { 
			case KeyEvent.VK_UP:
				if (!directions.contains(Direction.UP)) {
					directions.add(Direction.UP);
				}
				break;
			case KeyEvent.VK_DOWN:
				if (!directions.contains(Direction.DOWN)) {
					directions.add(Direction.DOWN);
				}
				break;
			case KeyEvent.VK_LEFT:
				if (!directions.contains(Direction.LEFT)) {
					directions.add(Direction.LEFT);
				}
				break;
			case KeyEvent.VK_RIGHT :
				if (!directions.contains(Direction.RIGHT)) {
					directions.add(Direction.RIGHT);
				}
				break;
			case KeyEvent.VK_SPACE :
				camera.center();
				break;
			}

			cameraMovementHandler.actionPerformed(null);
		}

		/**
		 * Removes direction from list once key has been released
		 * @Override
		 */
		public void keyReleased(KeyEvent e) {
			int keyCode = e.getKeyCode();
			//System.out.println("released");
			switch( keyCode ) { 
			case KeyEvent.VK_UP:
				directions.remove(Direction.UP);
				break;
			case KeyEvent.VK_DOWN:
				directions.remove(Direction.DOWN);
				break;
			case KeyEvent.VK_LEFT:
				directions.remove(Direction.LEFT);
				break;
			case KeyEvent.VK_RIGHT :
				directions.remove(Direction.RIGHT);
				break;
			}

			cameraMovementHandler.actionPerformed(null);
		}

		@Override
		public void keyTyped(KeyEvent e) {

		}
	}

	/**
	 * Moves the camera at a set speed using a Timer
	 * @author Matthew Zane
	 * @version 1.1
	 * @since 2017-09-15
	 */
	private class CameraMovementHandler implements ActionListener {

		/**
		 * Moves and updates Camera
		 */
		public void actionPerformed(ActionEvent arg0) {
			move();
			camera.updateCamera();
		}

		/**
		 * Moves camera depending on directions
		 */
		public void move() {
			//System.out.println("Moving");
			if (directions.contains(Direction.UP)) {
				moveUp();
			}
			if (directions.contains(Direction.DOWN)) {
				moveDown();
			}
			if (directions.contains(Direction.LEFT)) {
				moveLeft();
			}
			if (directions.contains(Direction.RIGHT)) {
				moveRight();
			}
		}

		/**
		 * Moves camera up
		 */
		public void moveUp()  {
			camera.moveUp();
		}

		/**
		 * Moves camera down
		 */
		public void moveDown() {
			camera.moveDown();
		}

		/**
		 * Moves camera left
		 */
		public void moveLeft() {
			camera.moveLeft();
		}

		/**
		 * Moves camera right
		 */
		public void moveRight() {
			camera.moveRight();
		}
	}

	/**
	 * Handles mouse clicks on panel
	 * @author Matthew Zane
	 * @version 1.1
	 * @since 2017-09-15
	 */
	public class MouseHandler implements MouseListener {

		/**
		 * Panel requests focus when clicked on
		 * @Override
		 */
		public void mouseClicked(MouseEvent e) {
			if (getMousePosition() != null) {
				requestFocus();
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub

		}
	}
}
