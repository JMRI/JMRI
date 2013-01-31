 // YardmasterFrame.java

package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarSetFrame;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Yardmaster Frame.  Shows work at one location.
 * 
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 18630 $
 */

public class YardmasterFrame extends OperationsFrame implements java.beans.PropertyChangeListener {
	
	Location _location = null;
	Train _train = null;
	int _visitNumber = 1;

	TrainManager trainManager = TrainManager.instance();
	EngineManager engManager = EngineManager.instance();
	CarManager carManager = CarManager.instance();
	TrainCommon trainCommon = new TrainCommon();
	
	JScrollPane locoPane;
	JScrollPane pickupPane;
	JScrollPane setoutPane;
	JScrollPane movePane;

	// labels
	JLabel textStatus = new JLabel();
	JLabel textRailRoadName = new JLabel(Setup.getRailroadName());
//	JLabel textTrainName = new JLabel();
	JLabel textTrainDescription = new JLabel();
	JLabel textTrainComment = new JLabel();
	JLabel textTrainRouteComment = new JLabel();
	JLabel textTrainRouteLocationComment = new JLabel();
	JLabel textLocationComment = new JLabel();
	JLabel textLocationName = new JLabel();

	// major buttons
	JButton selectButton = new JButton(Bundle.getMessage("Select"));
	JButton clearButton = new JButton(Bundle.getMessage("Clear"));
	JButton setButton = new JButton(Bundle.getMessage("Set"));

	// radio buttons
	
	// text field
	
	// combo boxes
	JComboBox trainComboBox = new JComboBox();
	JComboBox trainVisitComboBox = new JComboBox();
	
	// panels
	JPanel pTrainVisit = new JPanel();
   	JPanel pTrainComment = new JPanel();
   	JPanel pTrainRouteComment = new JPanel();
	JPanel pPickupLocos = new JPanel();
	JPanel pSetoutLocos = new JPanel();
	JPanel pPickups = new JPanel();
	JPanel pSetouts = new JPanel();
	JPanel pMoves = new JPanel();
	JPanel pTrainRouteLocationComment = new JPanel();
  	JPanel pWork = new JPanel();
	
	// check boxes
	Hashtable<String, JCheckBox> carCheckBoxes = new Hashtable<String, JCheckBox>();
	List<RollingStock> rollingStock = new ArrayList<RollingStock>();
	
	// flags
	boolean setMode = false;	// when true, cars that aren't selected can be "set"


	public YardmasterFrame() {
		super(Bundle.getMessage("Yardmaster"));
	}

	public void initComponents(Location location) {
		_location = location;

	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
	    JPanel p = new JPanel();
	    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
	    p.add(pPickupLocos);
	    p.add(pSetoutLocos);
   
       	locoPane = new JScrollPane(p);
       	locoPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Engines")));
       	locoPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
       	
       	pickupPane = new JScrollPane(pPickups);
       	pickupPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Pickup")));
       	pickupPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 //      	pickupPane.setPreferredSize(new Dimension(200, 300));
       	
      	setoutPane = new JScrollPane(pSetouts);
      	setoutPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SetOut")));
      	setoutPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
//      	setoutPane.setPreferredSize(new Dimension(200, 300));
      	
      	movePane = new JScrollPane(pMoves);
      	movePane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocalMoves")));
      	movePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

	    //      Set up the panels
      	pPickupLocos.setLayout(new BoxLayout(pPickupLocos,BoxLayout.Y_AXIS));
      	pSetoutLocos.setLayout(new BoxLayout(pSetoutLocos,BoxLayout.Y_AXIS));
       	pPickups.setLayout(new BoxLayout(pPickups,BoxLayout.Y_AXIS));
       	pSetouts.setLayout(new BoxLayout(pSetouts,BoxLayout.Y_AXIS));
       	pMoves.setLayout(new BoxLayout(pMoves,BoxLayout.Y_AXIS));
 
				
		// Layout the panel by rows
		
       	// row 2
       	JPanel pRow2 = new JPanel();
       	pRow2.setLayout(new BoxLayout(pRow2,BoxLayout.X_AXIS));
       	
       	// row 2a (location name)
       	JPanel pLocationName = new JPanel();
       	pLocationName.setBorder(BorderFactory.createTitledBorder("Location"));
       	pLocationName.add(textLocationName);
       	
		// row 2b (railroad name)
       	JPanel pRailRoadName = new JPanel();
       	pRailRoadName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RailroadName")));
       	pRailRoadName.add(textRailRoadName);
        	
       	pRow2.add(pLocationName);
       	pRow2.add(pRailRoadName);
       	
       	// row 4 (location comment)
    	JPanel pLocationComment = new JPanel();
       	pLocationComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocationComment")));
       	pLocationComment.add(textLocationComment);

       	// row 6
       	JPanel pRow6 = new JPanel();
       	pRow6.setLayout(new BoxLayout(pRow6,BoxLayout.X_AXIS));

		// row 6a (train name)
       	JPanel pTrainName = new JPanel();
       	pTrainName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
       	pTrainName.add(trainComboBox);
       	
       	// row 6b (train visit)
       	pTrainVisit.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Visit")));
       	pTrainVisit.add(trainVisitComboBox);
       	
		// row 6c (train description)
       	JPanel pTrainDescription = new JPanel();
       	pTrainDescription.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Description")));
       	pTrainDescription.add(textTrainDescription);
       	
       	pRow6.add(pTrainName);
       	pRow6.add(pTrainVisit);
       	pRow6.add(pTrainDescription);

       	// row 8 (train comment)
       	pTrainComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainComment")));
       	pTrainComment.add(textTrainComment);
       	
      	// row 10 (train route comment)
       	pTrainRouteComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RouteComment")));
       	pTrainRouteComment.add(textTrainRouteComment);
 
       	// row 11 (train route location comment)
       	pTrainRouteLocationComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RouteLocationComment")));
       	pTrainRouteLocationComment.add(textTrainRouteLocationComment);
      	
       	// row 12
       	JPanel pWorkPanes = new JPanel();
       	pWorkPanes.setLayout(new BoxLayout(pWorkPanes,BoxLayout.X_AXIS));
      	
       	pWorkPanes.add(pickupPane);
       	pWorkPanes.add(setoutPane);
       	
       	// row 13
      	JPanel pStatus = new JPanel();
      	pStatus.setLayout(new GridBagLayout());
      	pStatus.setBorder(BorderFactory.createTitledBorder(""));
       	addItem(pStatus, textStatus, 0, 0);
       	       	
       	// row 14
      	pWork.setLayout(new GridBagLayout());
      	pWork.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Work")));
      	pWork.setMaximumSize(new Dimension(2000, 200));
       	addItem(pWork, selectButton, 0, 0);
       	addItem(pWork, clearButton, 1, 0);
       	addItem(pWork, setButton, 2, 0);
       			
		getContentPane().add(pRow2);
		getContentPane().add(pLocationComment);
		getContentPane().add(pRow6);
		getContentPane().add(pTrainComment);
		getContentPane().add(pTrainRouteComment);
		getContentPane().add(pTrainRouteLocationComment);
		getContentPane().add(locoPane);
		getContentPane().add(pWorkPanes);
		getContentPane().add(movePane);
		getContentPane().add(pStatus);
		getContentPane().add(pWork);		
		
		// setup buttons
       	addButtonAction(selectButton);
       	addButtonAction(clearButton);
		addButtonAction(setButton);
				
		// tool tips
		
		if (_location != null) {
			textLocationName.setText(_location.getName());
			textLocationComment.setText(_location.getComment());
			pLocationComment.setVisible(!_location.getComment().equals(""));
			updateTrainsComboBox();
		} 
		
       	update();
		
		addComboBoxAction(trainComboBox);
		addComboBoxAction(trainVisitComboBox);	

		addHelpMenu("package.jmri.jmrit.operations.Operations_Locations", true); // NOI18N
		
		setMinimumSize(new Dimension(600, Control.panelHeight));
		pack();
    	setVisible(true);
		
	}
	
	// Save, Delete, Add 
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == selectButton)
			selectCheckboxes(true);
		if (ae.getSource() == clearButton)
			selectCheckboxes(false);
		if (ae.getSource() == setButton){
			setMode = !setMode;	// toggle setMode
			update();
		}
	}
	
	CarSetFrame csf = null;
	public void setCarButtonActionPerfomed(java.awt.event.ActionEvent ae) {
		String name = ((JButton)ae.getSource()).getName();
		log.debug("Set button for car "+ name);
		Car car = carManager.getById(name);
       	if (csf != null)
       		csf.dispose();
   		csf = new CarSetFrame();
		csf.initComponents();
    	csf.loadCar(car);
//    	csf.setTitle(Bundle.getMessage("TitleCarSet"));
    	csf.setVisible(true);
    	csf.setExtendedState(Frame.NORMAL);
	}
	
	protected void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		// confirm that all work is done
		check();
	}
	
	protected void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == trainComboBox) {
			if (_train != null)
				_train.removePropertyChangeListener(this);
			_train = null;
			_visitNumber = 1;
			if (trainComboBox.getSelectedItem() != null && !trainComboBox.getSelectedItem().equals(""))
				_train = (Train) trainComboBox.getSelectedItem();
			// listen for train changes
			if (_train != null)
				_train.addPropertyChangeListener(this);
			update();
		}
		if (ae.getSource() == trainVisitComboBox) {
			if (trainVisitComboBox.getSelectedItem() != null && trainVisitComboBox.isVisible()) {
				_visitNumber = (Integer) trainVisitComboBox.getSelectedItem();
				update();
			}
		}
	}
	
	
	private void check(){
		Enumeration<JCheckBox> en = carCheckBoxes.elements();
		while (en.hasMoreElements()){
			JCheckBox checkBox = en.nextElement();
			if (!checkBox.isSelected()){
				log.debug("Checkbox ("+checkBox.getText()+") isn't selected ");
				setButton.setEnabled(true);
				return;
			}
		}
		// all selected, work done!
		setButton.setEnabled(false);
		setMode = false;
	}
	
	private void selectCheckboxes(boolean enable){
		Enumeration<JCheckBox> en = carCheckBoxes.elements();
		while (en.hasMoreElements()){
			JCheckBox checkBox = en.nextElement();
			checkBox.setSelected(enable);
		}
		setMode = false;
		update();
	}
	
	private void update() {
		log.debug("update, setMode " + setMode);
		removePropertyChangeListerners();
		pPickupLocos.removeAll();
		pSetoutLocos.removeAll();
		pPickups.removeAll();
		pSetouts.removeAll();
		pMoves.removeAll();
		
		pTrainComment.setVisible(false);
		pTrainRouteComment.setVisible(false);
		pTrainRouteLocationComment.setVisible(false);
		pickupPane.setVisible(false);
		setoutPane.setVisible(false);
		locoPane.setVisible(false);
		movePane.setVisible(false);
		pWork.setVisible(false);
		pTrainVisit.setVisible(false);
		trainVisitComboBox.setVisible(false);
		
		textTrainDescription.setText("");
		textStatus.setText("");
		
		setButtonText();
		
		if (_train != null && _train.getRoute() != null) {
			Route route = _train.getRoute();
			pWork.setVisible(true);
			textTrainDescription.setText(_train.getDescription());
			// show train comment box only if there's a comment
			if (!_train.getComment().equals("")) {
				pTrainComment.setVisible(true);
				textTrainComment.setText(_train.getComment());
			}
			// show route comment box only if there's a route comment
			if (!route.getComment().equals("")) {
				pTrainRouteComment.setVisible(true);
				textTrainRouteComment.setText(route.getComment());
			}
			// Does this train have a unique railroad name?
			if (!_train.getRailroadName().equals(""))
				textRailRoadName.setText(_train.getRailroadName());

			RouteLocation rl = null;
			
			boolean lastLocation = false;
			
			List<String> routeList = route.getLocationsBySequenceList();
			int visitNumber = 0;
			for (int i=0; i<routeList.size(); i++) {
				RouteLocation l = route.getLocationById(routeList.get(i));
				if (l.getName().equals(_location.getName())) {			
					visitNumber++;
					if (visitNumber == _visitNumber) {
						rl = l;
						if (i == routeList.size()-1)
							lastLocation = true;
					}
				}
			}			
			
			if (rl != null) {
				// update visit numbers
				if (visitNumber > 1) {
					trainVisitComboBox.removeAllItems();
					for (int i = 0; i < visitNumber; i++) {
						trainVisitComboBox.addItem(i+1);	
					}
					trainVisitComboBox.setSelectedItem(_visitNumber);
					trainVisitComboBox.setVisible(true);
					pTrainVisit.setVisible(true);
				}
				pTrainRouteLocationComment.setVisible(!rl.getComment().equals(""));
				textTrainRouteLocationComment.setText(rl.getComment());
				textLocationName.setText(rl.getLocation().getName());
				textLocationComment.setText(rl.getLocation().getComment());

				// check for locos
				List<String> engList = engManager.getByTrainList(_train);
				for (int k = 0; k < engList.size(); k++) {
					Engine engine = engManager.getById(engList.get(k));
					if (engine.getRouteLocation() == rl && !engine.getTrackName().equals("")) {
						locoPane.setVisible(true);
						rollingStock.add(engine);
						engine.addPropertyChangeListener(this);
						JCheckBox checkBox = new JCheckBox(trainCommon.pickupEngine(engine));
						setCheckBoxFont(checkBox);
						pPickupLocos.add(checkBox);
					}
					if (engine.getRouteDestination() == rl) {
						locoPane.setVisible(true);
						rollingStock.add(engine);
						engine.addPropertyChangeListener(this);
						JCheckBox checkBox = new JCheckBox(trainCommon.dropEngine(engine));
						setCheckBoxFont(checkBox);
						pSetoutLocos.add(checkBox);
					}
				}
				// now update the car pick ups and set outs
				List<String> carList = carManager.getByTrainDestinationList(_train);

				// block pick ups by destination
				for (int j = 0; j < routeList.size(); j++) {
					RouteLocation rld = route.getLocationById(routeList.get(j));
					for (int k = 0; k < carList.size(); k++) {
						Car car = carManager.getById(carList.get(k));
						if (car.getRouteLocation() == rl && !car.getTrackName().equals("")
								&& car.getRouteDestination() == rld && car.getRouteDestination() != rl) {
							pickupPane.setVisible(true);
							rollingStock.add(car);
							car.addPropertyChangeListener(this);
							if (carCheckBoxes.containsKey("p" + car.getId())) {
								if (setMode && !carCheckBoxes.get("p" + car.getId()).isSelected()) {
									// change to set button so user can remove car from train
									pPickups.add(addSet(car));
								} else {
									pPickups.add(carCheckBoxes.get("p" + car.getId()));
								}
							} else {
								JCheckBox checkBox = new JCheckBox(trainCommon.pickupCar(car));
								setCheckBoxFont(checkBox);
								addCheckBoxAction(checkBox);
								pPickups.add(checkBox);
								carCheckBoxes.put("p" + car.getId(), checkBox);
							}
						}
					}
				}
				// set outs
				for (int j = 0; j < carList.size(); j++) {
					Car car = carManager.getById(carList.get(j));
					if ((car.getRouteLocation() != rl && car.getRouteDestination() == rl
							&& !car.getTrackName().equals(""))
							|| (car.getTrackName().equals("") && car.getRouteDestination() == rl)) {
						setoutPane.setVisible(true);
						if (!rollingStock.contains(car)) {
							rollingStock.add(car);
							car.addPropertyChangeListener(this);
						}
						if (carCheckBoxes.containsKey("s" + car.getId())) {
							if (setMode && !carCheckBoxes.get("s" + car.getId()).isSelected()) {
								// change to set button so user can remove car from train
								pSetouts.add(addSet(car));
							} else {
								pSetouts.add(carCheckBoxes.get("s" + car.getId()));
							}
						} else {
							JCheckBox checkBox = new JCheckBox(trainCommon.dropCar(car));
							setCheckBoxFont(checkBox);
							addCheckBoxAction(checkBox);
							pSetouts.add(checkBox);
							carCheckBoxes.put("s" + car.getId(), checkBox);
						}
					}
				}
				// local moves
				for (int j = 0; j < carList.size(); j++) {
					Car car = carManager.getById(carList.get(j));
					if (car.getRouteLocation() == rl && car.getRouteDestination() == rl 
							&& !car.getTrackName().equals("")) {
						movePane.setVisible(true);
						if (!rollingStock.contains(car)) {
							rollingStock.add(car);
							car.addPropertyChangeListener(this);
						}
						if (carCheckBoxes.containsKey("m" + car.getId())) {
							if (setMode && !carCheckBoxes.get("m" + car.getId()).isSelected()) {
								// change to set button so user can remove car from train
								pMoves.add(addSet(car));
							} else {
								pMoves.add(carCheckBoxes.get("m" + car.getId()));
							}
						} else {
							JCheckBox checkBox = new JCheckBox(trainCommon.moveCar(car));
							setCheckBoxFont(checkBox);
							addCheckBoxAction(checkBox);
							pMoves.add(checkBox);
							carCheckBoxes.put("m" + car.getId(), checkBox);
						}
					}
				}
				if (lastLocation) {
					textStatus.setText(MessageFormat.format(Bundle.getMessage("TrainTerminatesIn"),
							new Object[] { _train.getTrainTerminatesName() }));
				} else {
					textStatus.setText(getStatus(rl));
				}
			}
			pPickupLocos.repaint();
			pSetoutLocos.repaint();
			pPickups.repaint();
			pSetouts.repaint();
			pMoves.repaint();

			pPickupLocos.validate();
			pSetoutLocos.validate();
			pPickups.validate();
			pSetouts.validate();
			pMoves.validate();
			selectButton.setEnabled(carCheckBoxes.size() > 0);
			clearButton.setEnabled(carCheckBoxes.size() > 0);
			check();
		}
	}
	
	private JPanel addSet(Car car){
      	JPanel pSet = new JPanel();
      	pSet.setLayout(new GridBagLayout());							      	
		JButton carSetButton = new JButton(Bundle.getMessage("Set"));
		carSetButton.setName(car.getId());
		carSetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				setCarButtonActionPerfomed(e);
			}
		});
		JLabel label = new JLabel(car.toString());
		addItem(pSet, label, 0,0);
		addItemLeft(pSet, carSetButton, 1,0);								
		return pSet;
	}
	
	private void setCheckBoxFont(JCheckBox checkBox){
		if (Setup.isTabEnabled()){			
			Font font = new Font ("Courier", Font.PLAIN, checkBox.getFont().getSize());
			checkBox.setFont(font);
		}
	}
	
	private void setButtonText(){
		if (setMode)
			setButton.setText(Bundle.getMessage("Done"));
		else
			setButton.setText(Bundle.getMessage("Set"));
	}
	
	private String getStatus(RouteLocation rl){
		return MessageFormat.format(Bundle.getMessage("TrainDepartsCars"),
				new Object[] { rl.getName(), rl.getTrainDirectionString(), _train.getNumberCarsInTrain(rl),
			_train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(), _train.getTrainWeight(rl) });
	}
	
	private void updateTrainsComboBox() {
		trainComboBox.removeAllItems();
		trainComboBox.addItem("");
		if (_location != null) {
			List<Train> trains = trainManager.getTrainsArrivingThisLocationList(_location);
			for (int i = 0; i < trains.size(); i++) {
				if(isThereWorkAtLocation(trains.get(i), _location))
					trainComboBox.addItem(trains.get(i));
			}
		}
	}
	
	// returns true if there's work at location
	private boolean isThereWorkAtLocation(Train train, Location location) {
		List<String> carList = carManager.getByTrainDestinationList(train);
		for (int i = 0; i < carList.size(); i++) {
			Car car = carManager.getById(carList.get(i));
			if (car.getRouteLocation().getName().equals(location.getName()) 
					|| car.getRouteDestination().getName().equals(location.getName()))
				return true;
		}
		List<String> engList = engManager.getByTrainList(train);
		for (int i = 0; i < engList.size(); i++) {
			Engine eng = engManager.getById(engList.get(i));
			if (eng.getRouteLocation().getName().equals(location.getName())
					|| eng.getRouteDestination().getName().equals(location.getName()))
				return true;
		}
		return false;
	}
    
    private void removePropertyChangeListerners(){
		for (int i=0; i<rollingStock.size(); i++){
			rollingStock.get(i).removePropertyChangeListener(this);
		}
		rollingStock.clear();
    }
	
	public void dispose() {
		removePropertyChangeListerners();
		if (_train != null){
			_train.removePropertyChangeListener(this);
		}
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e){
		//if (Control.showProperty && log.isDebugEnabled()) 
		log.debug("Property change " +e.getPropertyName() + " for: "+e.getSource().toString()
				+ " old: "+e.getOldValue()+ " new: "+e.getNewValue()); // NOI18N
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

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(YardmasterFrame.class.getName());
}
