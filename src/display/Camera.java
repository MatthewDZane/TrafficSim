package display;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import world.World;

/**
 * Represents the position of the world that the user can see
 * from along with the variables needed to translate and scale
 * the view into the panel. View is always a square portion of
 * World.
 * @author Matthew Zane
 * @version 1.1
 * @since 2017-09-15
 */
public class Camera {
	//The actual dimensions for the WorldPanel when PCT_OF_PARENT
	//is .90 (90%)
	public int actualPanelWidth = 884;
	public int actualPanelHeight = 863;

	private World world;
	private WorldPanel panel;

	//Variable used to fit view into panel
	private double scale = 1;
	private double xalign = 0;
	private double yalign = 0;

	private Rectangle cameraPosition = new Rectangle();
	
	//Activates the CameraInitialCenterListener every 0.1 sec
	private Timer centerTimer = new Timer(100, new CameraInitialCenterListener());;

	public double getScale() { return scale; }
	public void setScale(double scaleIn) { scale = scaleIn;	}
	public double getXalign() { return xalign; }
	public void setXalign(double xalignIn) { xalign = xalignIn;	}
	public double getYalign() { return yalign; }
	public void setYalign(double yalignIn) { yalign = yalignIn;	}

	/**
	 * Creates a camera and uses the World and WorldPanel to center
	 * the camera
	 * @param worldIn
	 * @param panelIn
	 */
	public Camera(World worldIn, WorldPanel panelIn) {
		world = worldIn;
		panel = panelIn;
		
		//System.out.println(panel.getHeight());
		centerTimer.start();
	}

	/**
	 * Centers camera around relevant graphics
	 */
	public void center() {
		actualPanelWidth = panel.getWidth();
		actualPanelHeight = panel.getHeight();

		cameraPosition = world.getBounds();

		updateCamera();
	}
	
	/**
	 * Zooms camera in or out
	 * @param zoomPercent - percent change in view size - 
	 * negative is a zoom in and positive is a zoom out
	 */
	public void zoom(double zoomPercent) {
		Rectangle newPosition = new Rectangle(
				(int)(Math.round(cameraPosition.x + cameraPosition.getWidth() / 2.0 
						- (100 + zoomPercent) / 200 * cameraPosition.width)), 
				(int)(Math.round(cameraPosition.y + cameraPosition.height / 2.0
						- (100 + zoomPercent) / 200 * cameraPosition.height)), 
				(int)(Math.round(cameraPosition.width * (100 + zoomPercent) / 100)),
				(int)(Math.round(cameraPosition.height * (100 + zoomPercent) / 100)));
		cameraPosition = newPosition;
		updateCamera();
	}

	/**
	 * Moves cameraPosition up by 10cm
	 */
	public void moveUp() {
		cameraPosition.y -= 10;
	}

	/**
	 * Moves cameraPosition down by 10cm
	 */
	public void moveDown() {
		cameraPosition.y += 10;
	}

	/**
	 * Moves cameraPosition left by 10cm
	 */
	public void moveLeft() {
		cameraPosition.x -= 10;
	}

	/**
	 * Moves cameraPosition right by 10cm
	 */
	public void moveRight() {
		cameraPosition.x += 10;
	}
	
	/**
	 * Uses cameraPosition to update scaling and translating variables.
	 * Note: Not sure why there is a width vs. height comparison.
	 */
	public void updateCamera() {
		if (cameraPosition.width > cameraPosition.height) {
			scale = actualPanelWidth / (double) cameraPosition.width;
			xalign = -cameraPosition.x * scale + 1;
			yalign = -(cameraPosition.y - (cameraPosition.width - cameraPosition.height) / 2 + 1) * scale;
		}
		else {
			scale = actualPanelHeight / (double) cameraPosition.height;
			yalign = -cameraPosition.y * scale + 1;
			xalign = -(cameraPosition.x - (cameraPosition.height - cameraPosition.width) / 2 + 1) * scale;
		}
	}
	
	/**
	 * ActionListener used to attempt to center the camera after
	 * initialization
	 * @author Matthew Zane
	 * @version 1.1
	 * @since 2017-09-15
	 */
	private class CameraInitialCenterListener implements ActionListener {

		/**
		 * Checks if panel has been created yet, i.e panel width and
		 * height are greater than 0. Stops the timer once centering
		 * has been successful.
		 * @Override
		 */
		public void actionPerformed(ActionEvent e) {
			if (panel.getHeight() > 0 && panel.getWidth() > 0) {
				center();
			}
			centerTimer.stop();
		}
		
	}
}
