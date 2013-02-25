 // TrainConductorFrame.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import jmri.jmrit.operations.CommonConductorYardmasterFrame;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;


/**
 * Conductor Frame.  Shows work for a train one location at a time.
 * 
 * @author Dan Boudreau Copyright (C) 2011, 2013
 * @version $Revision: 18630 $
 */

public class TrainConductorFrame extends CommonConductorYardmasterFrame {

	// labels
	JLabel textTrainName = new JLabel();
	JLabel textNextLocationName = new JLabel();

	// major buttons


	public TrainConductorFrame() {
		super();
	}

	public void initComponents(Train train) {
		super.initComponents();
		
		_train = train;
	    		
       	// row 2
       	JPanel pRow2 = new JPanel();
       	pRow2.setLayout(new BoxLayout(pRow2,BoxLayout.X_AXIS));
       	
		// row 2a (train name)
       	JPanel pTrainName = new JPanel();
       	pTrainName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
       	pTrainName.add(textTrainName);
       	
       	pRow2.add(pTrainName);
       	pRow2.add(pTrainDescription);
       	pRow2.add(pRailRoadName);
       	       	
       	// row 6
       	JPanel pRow6 = new JPanel();
       	pRow6.setLayout(new BoxLayout(pRow6,BoxLayout.X_AXIS));
       	       		       	
       	pRow6.add(pTrainRouteComment);	// train route comment
       	pRow6.add(pTrainRouteLocationComment); // train route location comment
       	
       	// row 10
       	JPanel pRow10 = new JPanel();
       	pRow10.setLayout(new BoxLayout(pRow10,BoxLayout.X_AXIS));
       	       	       	
      	// row 10c (next location name)
       	JPanel pNextLocationName = new JPanel();
       	pNextLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("NextLocation")));
       	pNextLocationName.add(textNextLocationName);
       	
       	pRow10.add(pLocationName); // location name
       	pRow10.add(pLocationComment);
       	pRow10.add(pNextLocationName);
       	
       	// row 14
       	JPanel pRow14 = new JPanel();
       	pRow14.setLayout(new BoxLayout(pRow14,BoxLayout.X_AXIS));
       	pRow14.setMaximumSize(new Dimension(2000, 200));
       	       	
       	// row 14b
      	JPanel pMoveButton = new JPanel();
      	pMoveButton.setLayout(new GridBagLayout());
      	pMoveButton.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
       	addItem(pMoveButton, moveButton, 1, 0);
       	
       	pRow14.add(pButtons);
       	pRow14.add(pMoveButton);
       	     
       	update();
		
		getContentPane().add(pRow2);
		getContentPane().add(pTrainComment);
		getContentPane().add(pRow6);
		getContentPane().add(pRow10);
		getContentPane().add(locoPane);
		getContentPane().add(pWorkPanes);
		getContentPane().add(movePane);
		getContentPane().add(pStatus);
		getContentPane().add(pRow14);		
		
		// setup buttons
		addButtonAction(moveButton);
		
		if (_train != null){
			textTrainDescription.setText(_train.getDescription());
			// show train comment box only if there's a comment
			if (_train.getComment().equals(""))
				pTrainComment.setVisible(false);
			else
				textTrainComment.setText(_train.getComment());
			// show route comment box only if there's a route comment
			if (_train.getRoute() != null)
				if (_train.getRoute().getComment().equals("") || !Setup.isPrintRouteCommentsEnabled())
					pTrainRouteComment.setVisible(false);
				else
					textTrainRouteComment.setText(_train.getRoute().getComment());
			
			// Does this train have a unique railroad name?
			if (!_train.getRailroadName().equals(""))
				textRailRoadName.setText(_train.getRailroadName());
			else
				textRailRoadName.setText(Setup.getRailroadName());

			setTitle(Bundle.getMessage("TitleTrainConductor") + " ("+_train.getName()+")");

			// listen for train changes
			_train.addPropertyChangeListener(this);
		}

		//	build menu
		JMenuBar menuBar = new JMenuBar();
		if (_train != null){
			JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
			toolMenu.add(new ShowCarsInTrainAction(Bundle.getMessage("MenuItemShowCarsInTrain"), _train));
			menuBar.add(toolMenu);
		}
		setJMenuBar(menuBar);
		addHelpMenu("package.jmri.jmrit.operations.Operations_Trains", true); // NOI18N
		
		pack();
    	setVisible(true);
		
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == moveButton) {
			_train.move();
			return;
		}
		super.buttonActionPerformed(ae);
		update();
	}

	private void clearAndUpdate(){
		trainCommon.clearUtilityCarTypes();	// reset the utility car counts
		carCheckBoxes.clear();
		isSetMode = false;
		update();
	}
	
	private void update(){
		log.debug("update, setMode "+isSetMode);
		initialize();
		if (_train != null && _train.getRoute() != null){
			RouteLocation rl = _train.getCurrentLocation();
			if (rl != null) {
				textTrainName.setText(_train.getIconName());
				pTrainRouteLocationComment.setVisible(!rl.getComment().equals(""));
				textTrainRouteLocationComment.setText(rl.getComment());
				textLocationName.setText(rl.getLocation().getName());
				pLocationComment.setVisible(!rl.getLocation().getComment().equals("")
						&& Setup.isPrintLocationCommentsEnabled());
				textLocationComment.setText(rl.getLocation().getComment());
				textNextLocationName.setText(_train.getNextLocationName());
								
				// check for locos
				updateLocoPanes(rl);
				
				// now update the car pick ups and set outs
				blockCars(rl, true);

				textStatus.setText(getStatus(rl));

			} else {
				textStatus.setText(MessageFormat.format(Bundle.getMessage("TrainTerminatesIn"), new Object[] { _train.getTrainTerminatesName()}));
				moveButton.setEnabled(false);
				setButton.setEnabled(false);
			}
			updateComplete();
		}
	}
	
	public void dispose() {
		removePropertyChangeListerners();
		if (_train != null){
			_train.removePropertyChangeListener(this);
		}
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e){
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName() + " for: "+e.getSource().toString()
				+ " old: "+e.getOldValue()+ " new: "+e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY))
			clearAndUpdate();
		if ((e.getPropertyName().equals(RollingStock.ROUTE_LOCATION_CHANGED_PROPERTY) && e.getNewValue() == null)
				|| (e.getPropertyName().equals(RollingStock.ROUTE_DESTINATION_CHANGED_PROPERTY) && e.getNewValue() == null)
				|| e.getPropertyName().equals(RollingStock.TRAIN_CHANGED_PROPERTY)){
			// remove car from list
			if (e.getSource().getClass().equals(Car.class)){
				Car car = (Car)e.getSource();
				carCheckBoxes.remove("p"+car.getId());
				carCheckBoxes.remove("s"+car.getId());
				carCheckBoxes.remove("m"+car.getId());
				log.debug("Car "+car.toString()+" removed from list");
			}
			update();
		}
	}

	static Logger log = LoggerFactory
	.getLogger(TrainConductorFrame.class.getName());
}
