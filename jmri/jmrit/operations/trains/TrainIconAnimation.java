// TrainIconAmination.java

package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.routes.RouteLocation;

/**
 * Provides simple animation for train icons.
 *   
 * @author Daniel Boudreau (C) Copyright 2009
 * @version $Revision: 1.2 $
 */
public class TrainIconAnimation extends Thread{
	
	TrainIcon trainIcon;
	RouteLocation rl;
	private static final int bump = 2;
	
	public TrainIconAnimation(TrainIcon trainIcon, RouteLocation rl){
		 this.trainIcon = trainIcon; 
		 this.rl = rl;
		 setName("TrainIconAnimation");
	}
	
	public void run() {
		log.debug("TrainIconAnimation starts");
		int x = trainIcon.getX();
		int y = trainIcon.getY();
		int newX = rl.getTrainIconX();
		int newY = rl.getTrainIconY();

		while (x<newX){		
			trainIcon.setLocation(x, y);
			x = x+bump;
			sleep();
		}
		while (x>newX){		
			trainIcon.setLocation(x, y);
			x = x-bump;
			sleep();
		}	
		while (y<newY){		
			trainIcon.setLocation(newX, y);
			y = y+bump;
			sleep();
		}
		while (y>newY){
			trainIcon.setLocation(newX, y);
			y = y-bump;
			sleep();
		}
		trainIcon.setLocation(newX, newY);
	}
	
	private void sleep(){
		try{
			sleep(3);
		}catch (InterruptedException e){

		}
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainIconAnimation.class.getName());

}
