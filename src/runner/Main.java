package runner;

import display.Display;
import helpers.Timer;

public class Main {
	public static void main(String [] args) {
		Display frame = new Display();
		
		boolean isDone = false;

		double frameCap = 1.0 / 60.0;

		double frameTime = 0;
		int frames = 0;

		double time = Timer.getTime();
		double unprocessed = 0;
		while (!isDone) {
			boolean canRender = false;

			double time2 = Timer.getTime();
			double passed = time2 - time;
			unprocessed += passed;
			frameTime += passed;

			time = time2;

			while (unprocessed >= frameCap) {
				unprocessed -= frameCap;
				canRender = true;


				if (frameTime >= 1.0) {
					frameTime = 0;
					System.out.println("FPS: " + frames);
					frames = 0;
				}
			}
			if (canRender) {
				frame.repaint();
				frames++;
			}

		}
	}
}
