// TrainIconAmination.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.routes.RouteLocation;

/**
 * Provides simple animation for train icons.
 * 
 * @author Daniel Boudreau (C) Copyright 2009, 2010
 * @version $Revision$
 */
public class TrainIconAnimation extends Thread {

	TrainIcon _trainIcon;
	RouteLocation _rl;
	TrainIconAnimation _previous;
	private static final int bump = 2;

	public TrainIconAnimation(TrainIcon trainIcon, RouteLocation rl, TrainIconAnimation previous) {
		_trainIcon = trainIcon;
		_rl = rl;
		_previous = previous;
		setName("TrainIconAnimation"); // NOI18N
	}

	public void run() {
		// we need to wait for any previous icon animation to complete
		if (_previous != null) {
			while (_previous.isAlive())
				sleep();
		}
		log.debug("TrainIconAnimation starts for train " + _trainIcon.getTrain().getName());
		int x = _trainIcon.getX();
		int y = _trainIcon.getY();
		int newX = _rl.getTrainIconX();
		int newY = _rl.getTrainIconY();

		while (x < newX) {
			_trainIcon.setLocation(x, y);
			x = x + bump;
			sleep();
		}
		while (x > newX) {
			_trainIcon.setLocation(x, y);
			x = x - bump;
			sleep();
		}
		while (y < newY) {
			_trainIcon.setLocation(newX, y);
			y = y + bump;
			sleep();
		}
		while (y > newY) {
			_trainIcon.setLocation(newX, y);
			y = y - bump;
			sleep();
		}
		// log.debug("Route location: "+_rl.getName()+" final icon location X: "+newX+" Y: "+newY);
		_trainIcon.setLocation(newX, newY);
	}

	private void sleep() {
		try {
			sleep(3);
		} catch (InterruptedException e) {

		}
	}

	static Logger log = LoggerFactory.getLogger(TrainIconAnimation.class
			.getName());

}
