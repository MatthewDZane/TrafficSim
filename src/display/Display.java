package display;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import world.World;

/**
 * Main JFrame from which graphics will be displayed.
 * @author Matthew Zane
 * @version 1.1
 * @since 2017-09-15
 */
public class Display extends JFrame {
	
	//private World world = new World();
	
	public Display() {
		super();
		
		//Display is utilizing the MigLayout
		setLayout(new MigLayout());
		
		//Panel from which the world will be displayed
		WorldPanel panel = new WorldPanel(new World());
		panel.createCamera();
		
		//WorldPanel centered in JFrame with a height and width
		//equal to a certain percent of the Display
		add(panel, "w " + WorldPanel.PCT_OF_PARENT + "%, h " + 
				WorldPanel.PCT_OF_PARENT + "%, align center");
		
		//Border Panels placed around World Panel
		//May have future use
		add(new JPanel(), "north, h " + (100 - WorldPanel.PCT_OF_PARENT) / 2 + "%");
		add(new JPanel(), "south, h " + (100 - WorldPanel.PCT_OF_PARENT) / 2 + "%");
		add(new JPanel(), "west, w " + (100 - WorldPanel.PCT_OF_PARENT) / 2 + "%");
		add(new JPanel(), "east, w " + (100 - WorldPanel.PCT_OF_PARENT) / 2 + "%");
		
		setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1000, 1000);
		
		panel.requestFocus();;
	}
	
	
}
