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
 * @version $Revision$
 */

public class TrainIcon extends LocoIcon {

	public TrainIcon(Editor editor) {
        // super ctor call to make sure this is an icon label
    	super(editor); 
    }
	
	// train icon tool tips are always enabled
	public void setShowTooltip(boolean set){_showTooltip = true;}
 
    /**
     * Pop-up only if right click and not dragged
     * return true if a popup item is set 
     */
	public boolean showPopUp(JPopupMenu popup) {
		if (train != null){
			popup.add(new AbstractAction(Bundle.getMessage("Move")) {
				public void actionPerformed(ActionEvent e) {
					train.move();
				}
			});
			popup.add(makeTrainRouteMenu()); 
			popup.add(new TrainConductorAction(Bundle.getMessage("TitleTrainConductor"), train));
			popup.add(new ShowCarsInTrainAction(Bundle.getMessage("MenuItemShowCarsInTrain"), train));
            if (!isEditable()) {
                popup.add(new AbstractAction("Set X&Y") {
                    public void actionPerformed(ActionEvent e) {
                        if(!train.setTrainIconCoordinates())
                            JOptionPane.showMessageDialog(null, "See Operations -> Settings to enable Set X&Y",
                                    "Set X&Y is disabled",
                                    JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
		}
		popup.add(new ThrottleAction("Throttle")); 
		popup.add(makeLocoIconMenu());
        if (!isEditable()) {
            getEditor().setRemoveMenu(this, popup);
        }
        return true;
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
			tf.getAddressPanel().setAddress(getConsistNumber(), false);	// use consist address
			if (JOptionPane.showConfirmDialog(null,
					"Send function commands to lead loco?", "Consist Throttle",
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				tf.getAddressPanel().setRosterEntry(entry);			 	// use lead loco address
			}			
		} else {
			tf.getAddressPanel().setRosterEntry(entry);
		}
		tf.toFront();
	}

	private JMenu makeTrainRouteMenu(){
		JMenu routeMenu = new JMenu(Bundle.getMessage("Route"));
		Route route = train.getRoute();
		if (route == null)
			return routeMenu;
		List<String> routeList = route.getLocationsBySequenceList();
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getByTrainList(train);
		for (int r=0; r<routeList.size(); r++){
			int pickupCars = 0;
			int dropCars = 0;
			String current = "     ";
			RouteLocation rl = route.getLocationById(routeList.get(r));
			if (train.getCurrentLocation() == rl)
				current = "-> "; // NOI18N
			for (int j=0; j<carList.size(); j++){
				Car car = carManager.getById(carList.get(j));
				if (car.getRouteLocation() == rl && !car.getTrackName().equals("")){
					pickupCars++;
				}
				if (car.getRouteDestination() == rl){
					dropCars++;
				}
			}
			String rText = "";
			String pickups = "";
			String drops = "";
			if (pickupCars > 0){
				pickups = " "+Bundle.getMessage("Pickup")+" " + pickupCars;
				if (dropCars > 0)
					drops = ", "+Bundle.getMessage("SetOut")+" " + dropCars;
			}
			else if (dropCars > 0)
				drops = " "+Bundle.getMessage("SetOut")+" " + dropCars;
			if (pickupCars > 0 || dropCars > 0)
				rText = current + rl.getName() +"  (" + pickups + drops +" )";
			else
				rText = current + rl.getName();
			routeMenu.add(new RouteAction(rText, rl));
		}
		return routeMenu;
	}

	public class ThrottleAction extends AbstractAction {
	    public ThrottleAction(String actionName) {
	        super(actionName);
			if (entry == null)
				setEnabled(false);
	    }
		public void actionPerformed(ActionEvent e) {
			createThrottle();
		}
	}
	
	/**
	 * Moves train from current location to the one selected by user.
	 *
	 */
	public class RouteAction extends AbstractAction{
		RouteLocation _rl;
		public RouteAction(String actionName, RouteLocation rl) {
			super(actionName);
			_rl = rl;
		}
		public void actionPerformed(ActionEvent e) {
			log.debug("Route location selected "+_rl.getName());
			Route route = train.getRoute();
			List<String> routeList = route.getLocationsBySequenceList();
			// determine where the train is in the route
			for (int r=0; r<routeList.size(); r++){
				RouteLocation rl = route.getLocationById(routeList.get(r));
				if (train.getCurrentLocation() == rl){
					log.debug("Train is at location "+rl.getName());
					// Is train at this route location?
					if (rl == _rl)
						break;
					for (int i=r+1; i<routeList.size(); i++){
						RouteLocation nextRl = route.getLocationById(routeList.get(i));
						// did user select the next location in the route?
						if (nextRl == _rl && i == r+1){
							train.move();
						}else if (nextRl == _rl){
							if (JOptionPane.showConfirmDialog(null,
									"Move train to "+_rl.getName()+"?", "Move Train "+train.getIconName()+"?",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								while (train.getCurrentLocation() != _rl){
									train.move();
								}
							}
						}
					}
				}
			}
		}
	}
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoIcon.class.getName());
}
