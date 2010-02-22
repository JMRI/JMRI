package jmri.jmrit.operations.trains;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;

/**
 * An icon that displays the position of a train icon on a panel.<P>
 * The icon can always be repositioned and its popup menu is
 * always active.
 * @author Bob Jacobsen  Copyright (c) 2002
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.16 $
 */

public class TrainIcon extends LocoIcon {

 
	public TrainIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
    	super(editor); 
    }
	
	// train icon tool tips are always enabled
	public void setShowTooltip(boolean set){super.setShowTooltip(true);}
 
    /**
     * Pop-up only if right click and not dragged 
     */
	public void showPopUp(JPopupMenu popup) {
		//super.showPopUp(popup);

		if (train != null){
			popup.add(new AbstractAction("Move") {
				public void actionPerformed(ActionEvent e) {
					train.move();
				}
			});
			popup.add(makeTrainRouteMenu()); 
			popup.add(new AbstractAction("Set X&Y") {
				public void actionPerformed(ActionEvent e) {
					if(!train.setTrainIconCoordinates())
						JOptionPane.showMessageDialog(null, "See Operations -> Settings to enable Set X&Y",
								"Set X&Y is disabled",
								JOptionPane.ERROR_MESSAGE);
				}
			});
		}
		popup.add(new ThrottleAction("Throttle")); 
		popup.add(makeLocoIconMenu());
	}

	Train train = null;

	public void setTrain (Train train){
		this.train = train;
	}

	public Train getTrain (){
		return train;
	}

	int consistNumber = 0;

	public void setConsistNumber (int cN){
		this.consistNumber =   cN;
	}

	private int getConsistNumber(){
		return consistNumber;
	}

	jmri.jmrit.throttle.ThrottleFrame tf = null;

	private void createThrottle(){
		tf = jmri.jmrit.throttle.ThrottleFrameManager.instance().createThrottleFrame();
		if (getConsistNumber() > 0){
			if (JOptionPane.showConfirmDialog(null,
					"Send function commands to lead loco?", "Consist Throttle",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				tf.getAddressPanel().setRosterEntry(entry);			 	// first notify for func button
			}
			tf.getAddressPanel().setAddress(getConsistNumber(), false);	// second notify for consist address
		} else {
			tf.getAddressPanel().setRosterEntry(entry);
		}
		tf.toFront();
	}

	private JMenu makeTrainRouteMenu(){
		JMenu routeMenu = new JMenu("Route");
		Route route = train.getRoute();
		if (route == null)
			return routeMenu;
		List<String> routeList = route.getLocationsBySequenceList();
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getCarsByTrainList(train);
		for (int r=0; r<routeList.size(); r++){
			int pickupCars = 0;
			int dropCars = 0;
			String current = "     ";
			RouteLocation rl = route.getLocationById(routeList.get(r));
			if (train.getCurrentLocation() == rl)
				current = "-> ";
			for (int j=0; j<carList.size(); j++){
				Car car = carManager.getCarById(carList.get(j));
				if (car.getRouteLocation() == rl && !car.getTrackName().equals("")){
					pickupCars++;
				}
				if (car.getRouteDestination() == rl){
					dropCars++;
				}
			}
			String pickups = "";
			String drops = "";
			if (pickupCars > 0)
				pickups = " Pickups " + pickupCars;
			if (dropCars > 0)
				drops = " Drops " + dropCars;
			if (pickupCars > 0 || dropCars > 0)
				routeMenu.add(current + rl.getName() +"  (" + pickups + drops +" )");
			else
				routeMenu.add(current + rl.getName());
		}
		return routeMenu;
	}

	public class ThrottleAction extends AbstractAction {	
	    public ThrottleAction(String actionName) {
	        super(actionName);
	        // only enable if connected to an NCE system
			if (entry == null)
				setEnabled(false);
	    }
		public void actionPerformed(ActionEvent e) {
			createThrottle();
		}
	}
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoIcon.class.getName());
}
