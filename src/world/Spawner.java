package world;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import world.Road.SpawnerActionListener;

public class Spawner extends RoadEndObject {
	private SpawnerActionListener listener;
	private SpawnTimerListener spawnTimerListener = new SpawnTimerListener();

	private int lane;

	public Spawner(SpawnerActionListener listenerIn, int laneIn) {
		listener = listenerIn;

		lane = laneIn;

		Timer spawnTimer = new Timer(1000 / 2, spawnTimerListener);
		spawnTimer.setInitialDelay((int) (Math.random() * 10000 + 1));
		spawnTimer.start();
	}

	public void spawnCar() {
		int randomNum = (int) (Math.random() * 100 + 1);
		if (randomNum >= 1 && randomNum <= 15)
		listener.spawnCar(lane);
	}

	public class SpawnTimerListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			spawnCar();
		}

	}
}
