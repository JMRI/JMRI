// EngineSetFrame.java

package jmri.jmrit.operations.rollingstock.engines;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user to place engine on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.10 $
 */

public class EngineSetFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	EngineManager manager = EngineManager.instance();
	EngineManagerXml managerXml = EngineManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();
	TrainManager trainManager = TrainManager.instance();
	
	Engine _engine;
		
	// labels
	JLabel textEngineRoad = new JLabel();
	JLabel textEngineType = new JLabel();
	JLabel textName = new JLabel(rb.getString("Name"));
	JLabel textTrack = new JLabel(rb.getString("Track"));
	JLabel textName2 = new JLabel(rb.getString("Name"));
	JLabel textTrack2 = new JLabel(rb.getString("Track"));

	// major buttons	
	JButton saveButton = new JButton(rb.getString("Save"));
	
	// combo boxes
	JComboBox locationBox = LocationManager.instance().getComboBox();
	JComboBox trackLocationBox = new JComboBox(); 
	JComboBox destinationBox = LocationManager.instance().getComboBox();
	JComboBox trackDestinationBox = new JComboBox(); 
	JComboBox trainBox = TrainManager.instance().getComboBox();
	
	// check boxes
	JCheckBox outOfServiceCheckBox = new JCheckBox(rb.getString("OutOfService"));
		
	public EngineSetFrame() {
		super();
	}

	public void initComponents() {
		// the following code sets the frame's initial state	
		// create panel
		JPanel pPanel = new JPanel();
		pPanel.setLayout(new BoxLayout(pPanel,BoxLayout.Y_AXIS));
		
		// Layout the panel by rows
		// row 1
		JPanel pRow1 = new JPanel();
		pRow1.setLayout(new BoxLayout(pRow1,BoxLayout.X_AXIS));
		// row 1a
		JPanel pEngine = new JPanel();
		pEngine.setLayout(new GridBagLayout());
		pEngine.setBorder(BorderFactory.createTitledBorder(rb.getString("Engine")));
		addItem(pEngine, textEngineRoad, 1, 0);
		pRow1.add(pEngine);
		
		// row 1b
		JPanel pType = new JPanel();
		pType.setLayout(new GridBagLayout());
		pType.setBorder(BorderFactory.createTitledBorder(rb.getString("Type")));
		addItem(pType, textEngineType, 1, 0);
		pRow1.add(pType);
		
		// row 1c
		JPanel pStatus = new JPanel();
		pStatus.setLayout(new GridBagLayout());
		pStatus.setBorder(BorderFactory.createTitledBorder(rb.getString("Status")));
		addItem(pStatus, outOfServiceCheckBox, 1, 1);
		pRow1.add(pStatus);
		
		pPanel.add(pRow1);
		
		// row 2
		JPanel pLocation = new JPanel();
		pLocation.setLayout(new GridBagLayout());
		pLocation.setBorder(BorderFactory.createTitledBorder(rb.getString("Location")));
		addItem(pLocation, textName, 1, 0);
		addItem(pLocation, textTrack, 2, 0);
		
		// row 3
		addItem(pLocation, locationBox, 1, 1);
		addItem(pLocation, trackLocationBox, 2, 1);
		pPanel.add(pLocation);
		
		// optional panel
		JPanel pOptional = new JPanel();
		pOptional.setLayout(new BoxLayout(pOptional,BoxLayout.Y_AXIS));
		pOptional.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptionalProgram")));

		// row 6
		JPanel pDestination = new JPanel();
		pDestination.setLayout(new GridBagLayout());
		pDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("Destination")));
		addItem(pDestination, textName2, 1, 0);
		addItem(pDestination, textTrack2, 2, 0);
		addItem(pDestination, destinationBox, 1, 1);
		addItem(pDestination, trackDestinationBox, 2, 1);
		pOptional.add(pDestination);

		// row 8
		JPanel pTrain = new JPanel();
		pTrain.setLayout(new GridBagLayout());
		pTrain.setBorder(BorderFactory.createTitledBorder(rb.getString("Train")));
		addItem(pTrain, trainBox, 0, 0);
		pOptional.add(pTrain);

		// button panel
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		addItem(pButtons, saveButton, 2, 10);
		
		// add panels
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		getContentPane().add(pPanel);
		getContentPane().add(pOptional);
		getContentPane().add(pButtons);
		
		// setup buttons
		addButtonAction(saveButton);
		
		// setup combobox
		addComboBoxAction(locationBox);
		addComboBoxAction(destinationBox);

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);

		//	 get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);
		// get notified if train combo box gets modified
		trainManager.addPropertyChangeListener(this);
		
		// set frame size and location for display
		pack();
		if ( (getWidth()<400)) 
			setSize(450, getHeight()+50);
		else
			setSize (getWidth()+50, getHeight()+50);
		setLocation(Control.panelX, Control.panelY);
		setVisible(true);
	}
	
	public void loadEngine(Engine engine){
		_engine = engine;
		engine.addPropertyChangeListener(this);
		textEngineRoad.setText(engine.getRoad()+" "+engine.getNumber());
		textEngineType.setText(engine.getType());
		outOfServiceCheckBox.setSelected(engine.isOutOfService());
		updateComboBoxes();
		if (_engine.getRouteLocation() != null)
			log.debug("engine has a pickup location "+_engine.getRouteLocation().getName());
		if (_engine.getRouteDestination() != null)
			log.debug("engine has a destination "+_engine.getRouteDestination().getName());
		// has the program generated a pickup and drop for this engine?
		if (_engine.getRouteLocation() != null || _engine.getRouteDestination() != null){
			JOptionPane.showMessageDialog(this,
					rb.getString("pressSaveWill"),	rb.getString("engineInRoute"),
					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private void updateComboBoxes(){
		locationManager.updateComboBox(locationBox);
		locationBox.setSelectedItem(_engine.getLocation());
		Location l = _engine.getLocation();
		if (l != null){
			l.updateComboBox(trackLocationBox);
			trackLocationBox.setSelectedItem(_engine.getTrack());
		}		
		locationManager.updateComboBox(destinationBox);
		destinationBox.setSelectedItem(_engine.getDestination());
		l = _engine.getDestination();
		if (l != null){
			l.updateComboBox(trackDestinationBox);
			trackDestinationBox.setSelectedItem(_engine.getDestinationTrack());
		}
		trainManager.updateComboBox(trainBox);
		trainBox.setSelectedItem(_engine.getTrain());
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== locationBox){
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")){
					trackLocationBox.removeAllItems();
				}else{
					log.debug("EngineSetFrame sees location: "+ locationBox.getSelectedItem());
					Location l = (Location)locationBox.getSelectedItem();
					l.updateComboBox(trackLocationBox);
				}
			}
		}
		if (ae.getSource()== destinationBox){
			if (destinationBox.getSelectedItem() != null){
				if (destinationBox.getSelectedItem().equals("")){
					trackDestinationBox.removeAllItems();
				}else{
					log.debug("EngineSetFrame sees destination: "+ destinationBox.getSelectedItem());
					Location l = (Location)destinationBox.getSelectedItem();
					l.updateComboBox(trackDestinationBox);
				}
			}
		}
	}
	
	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {
			// save the statuses
			_engine.setOutOfService(outOfServiceCheckBox.isSelected());
			if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
				_engine.setLocation(null, null);
			} else {
				if (trackLocationBox.getSelectedItem() == null
						|| trackLocationBox.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(this,
							rb.getString("engineFullySelect"), rb.getString("engineCanNotLoc"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = _engine.setLocation((Location) locationBox.getSelectedItem(),
						(Track)trackLocationBox.getSelectedItem());
				if (!status.equals(Engine.OKAY)){
					log.debug ("Can't set engine's location because of "+ status);
					JOptionPane.showMessageDialog(this,
							rb.getString("engineCanNotLocMsg")+ status, rb.getString("engineCanNotLoc"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (destinationBox.getSelectedItem() == null || destinationBox.getSelectedItem().equals("")) {
				_engine.setDestination(null, null);
			} else {
				if (trackDestinationBox.getSelectedItem() == null
						|| trackDestinationBox.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(this,
							rb.getString("engineFullyDest"), rb.getString("engineCanNotDest"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = _engine.setDestination((Location) destinationBox.getSelectedItem(),
						(Track)trackDestinationBox.getSelectedItem());
				if (!status.equals(Engine.OKAY)){
					log.debug ("Can't set engine's destination because of "+ status);
					JOptionPane.showMessageDialog(this,
							rb.getString("engineCanNotDestMsg")+ status, rb.getString("engineCanNotDest"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals(""))
				_engine.setTrain(null);
			else {
				_engine.setTrain((Train)trainBox.getSelectedItem());
				// determine if train services this engine's type
				Train train = _engine.getTrain();
				if (train != null && !train.acceptsTypeName(_engine.getType())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("engineTrainNotServ"), new Object[]{_engine.getType(), train.getName()}),
							rb.getString("engineNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}		
				// determine if train services the location and destination selected by user
				RouteLocation rl = null;
				RouteLocation rd = null;
				Route route = null;
				if (train != null){
					route = train.getRoute();
					if (route != null){
						rl = route.getLastLocationByName(_engine.getLocationName());
						rd = route.getLastLocationByName(_engine.getDestinationName());
					}
				} else {
					log.error("Expected a train from combobox");
					return;
				}
				if (rl == null){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("engineLocNotServ"), new Object[]{_engine.getLocationName(), train.getName()}),
							rb.getString("engineNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (rd == null && !_engine.getDestinationName().equals("")){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("engineDestNotServ"), new Object[]{_engine.getDestinationName(), train.getName()}),
							rb.getString("engineNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				} 
				if (rd != null && route != null){
					// now determine if destination is after location
					List<String> routeSequence = route.getLocationsBySequenceList();
					boolean foundLoc = false;	// when true, found the engine's location in the route
					boolean foundDes = false;
					for (int i=0; i<routeSequence.size(); i++){
						String locId = routeSequence.get(i);
						RouteLocation location = route.getLocationById(locId);
						if (rl.getName().equals(location.getName())){
							foundLoc = true;
						}
						if (rd.getName().equals(location.getName()) && foundLoc){
							foundDes = true;
							break;
						}
						
					}
					if (!foundDes) {
						JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("engineLocOrder"),
										new Object[] {_engine.getDestinationName(),	_engine.getLocationName(),
										train.getName() }), rb.getString("engineNotMove"),
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			// is this engine part of a consist?
			if (_engine.getConsist() != null){
				if (JOptionPane.showConfirmDialog(this,
						rb.getString("engineInConsist"),
						rb.getString("enginePartConsist"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					List<Engine> engines = _engine.getConsist().getEngines();
					for(int i=0; i<engines.size(); i++){
						Engine engine = engines.get(i);
						if (engine == _engine)
							continue;
						if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
							engine.setLocation(null, null);
						} else {
							String status = engine.setLocation((Location) locationBox.getSelectedItem(),
								(Track)trackLocationBox.getSelectedItem());
							if (!status.equals(Engine.OKAY)){
								log.debug ("Can't set the location for all of the engines in the consist because of "+ status);
								JOptionPane.showMessageDialog(this,
										rb.getString("engineCanNotLocMsg")+ status,
										rb.getString("engineCanNotLoc"),
										JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						if (destinationBox.getSelectedItem() == null || destinationBox.getSelectedItem().equals("")) {
							engine.setDestination(null, null);
						} else {
							String status = engine.setDestination((Location) destinationBox.getSelectedItem(),
									(Track)trackDestinationBox.getSelectedItem());
							if (!status.equals(Engine.OKAY)){
								log.debug ("Can't set the destination for all of the engines in the consist because of "+ status);
								JOptionPane.showMessageDialog(this,
										rb.getString("engineCanNotDestMsg")+ status,
										rb.getString("engineCanNotDest"),
										JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals("")){
							engine.setTrain(null);
						} else {
							engine.setTrain((Train)trainBox.getSelectedItem());
						}
						// remove engine from being picked up and delivered
						engine.setRouteLocation(null);
						engine.setRouteDestination(null);
					}
				}
			}
			// remove engine from being picked up and delivered
			_engine.setRouteLocation(null);
			_engine.setRouteDestination(null);
			managerXml.writeOperationsFile();
		}
	}

	public void dispose(){
		if (_engine != null)
			_engine.removePropertyChangeListener(this);
		LocationManager.instance().removePropertyChangeListener(this);
		trainManager.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("EngineSetFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getSource().getClass().equals(Engine.class)){
			updateComboBoxes();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(EngineSetFrame.class.getName());
}
