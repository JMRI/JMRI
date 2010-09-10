// CarSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user to place car on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010
 * @version $Revision: 1.18 $
 */

public class CarSetFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();
	TrainManager trainManager = TrainManager.instance();
	
	Car _car;
		
	// labels
	JLabel textCarRoad = new JLabel();
	JLabel textCarType = new JLabel();
	JLabel textName = new JLabel(rb.getString("Name"));
	JLabel textTrack = new JLabel(rb.getString("Track"));
	JLabel textName2 = new JLabel(rb.getString("Name"));
	JLabel textTrack2 = new JLabel(rb.getString("Track"));
	JLabel textName3 = new JLabel(rb.getString("Name"));
	JLabel textTrack3 = new JLabel(rb.getString("Track"));
	JLabel textName4 = new JLabel(rb.getString("Name"));
	JLabel textTrack4 = new JLabel(rb.getString("Track"));

	// major buttons
	JButton saveButton = new JButton(rb.getString("Save"));
	
	// combo boxes
	JComboBox locationBox = LocationManager.instance().getComboBox();
	JComboBox trackLocationBox = new JComboBox(); 
	JComboBox destinationBox = LocationManager.instance().getComboBox();
	JComboBox trackDestinationBox = new JComboBox(); 
	JComboBox destReturnWhenEmptyBox = LocationManager.instance().getComboBox();
	JComboBox trackReturnWhenEmptyBox = new JComboBox(); 
	JComboBox nextDestinationBox = LocationManager.instance().getComboBox();
	JComboBox nextDestTrackBox = new JComboBox(); 
	JComboBox trainBox = TrainManager.instance().getComboBox();
	
	// check boxes
	JCheckBox autoTrackCheckBox = new JCheckBox(rb.getString("Auto"));
	JCheckBox autoDestinationTrackCheckBox = new JCheckBox(rb.getString("Auto"));
	JCheckBox autoNextDestTrackCheckBox = new JCheckBox(rb.getString("Auto"));
	JCheckBox autoReturnWhenEmptyTrackCheckBox = new JCheckBox(rb.getString("Auto"));
	JCheckBox locationUnknownCheckBox = new JCheckBox(rb.getString("LocationUnknown"));
		
	public CarSetFrame() {
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
		JPanel pCar = new JPanel();
		pCar.setLayout(new GridBagLayout());
		pCar.setBorder(BorderFactory.createTitledBorder(rb.getString("Car")));
		addItem(pCar, textCarRoad, 1, 0);
		pRow1.add(pCar);
		
		// row 1b
		JPanel pType = new JPanel();
		pType.setLayout(new GridBagLayout());
		pType.setBorder(BorderFactory.createTitledBorder(rb.getString("Type")));
		addItem(pType, textCarType, 1, 0);
		pRow1.add(pType);
		
		// row 1c
		JPanel pStatus = new JPanel();
		pStatus.setLayout(new GridBagLayout());
		pStatus.setBorder(BorderFactory.createTitledBorder(rb.getString("Status")));
		addItem(pStatus, locationUnknownCheckBox, 1, 0);
		pRow1.add(pStatus);
		
		pPanel.add(pRow1);
	
		// row 2
		JPanel pLocation = new JPanel();
		pLocation.setLayout(new GridBagLayout());
		pLocation.setBorder(BorderFactory.createTitledBorder(rb.getString("Location")));
		addItem(pLocation, textName, 1, 0);
		addItem(pLocation, textTrack, 2, 0);
		addItem(pLocation, locationBox, 1, 1);
		addItem(pLocation, trackLocationBox, 2, 1);
		addItem(pLocation, autoTrackCheckBox, 3, 1);
		pPanel.add(pLocation);
		
		// optional panel 1
		JPanel pOptional1 = new JPanel();
		pOptional1.setLayout(new BoxLayout(pOptional1,BoxLayout.Y_AXIS));
		pOptional1.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptional")));
		
		// row 5
		JPanel pReturnWhenEmpty = new JPanel();
		pReturnWhenEmpty.setLayout(new GridBagLayout());
		pReturnWhenEmpty.setBorder(BorderFactory.createTitledBorder(rb.getString("ReturnWhenEmpty")));
		addItem(pReturnWhenEmpty, textName4, 1, 0);
		addItem(pReturnWhenEmpty, textTrack4, 2, 0);
		addItem(pReturnWhenEmpty, destReturnWhenEmptyBox, 1, 1);
		addItem(pReturnWhenEmpty, trackReturnWhenEmptyBox, 2, 1);
		addItem(pReturnWhenEmpty, autoReturnWhenEmptyTrackCheckBox, 3, 1);
		pOptional1.add(pReturnWhenEmpty);
		
		// optional panel 2
		JPanel pOptional2 = new JPanel();
		pOptional2.setLayout(new BoxLayout(pOptional2,BoxLayout.Y_AXIS));
		pOptional2.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptionalProgram")));

		// row 6
		JPanel pDestination = new JPanel();
		pDestination.setLayout(new GridBagLayout());
		pDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("Destination")));
		addItem(pDestination, textName2, 1, 0);
		addItem(pDestination, textTrack2, 2, 0);
		addItem(pDestination, destinationBox, 1, 1);
		addItem(pDestination, trackDestinationBox, 2, 1);
		addItem(pDestination, autoDestinationTrackCheckBox, 3, 1);
		pOptional2.add(pDestination);
		
		// row 7
		JPanel pNextDestination = new JPanel();
		pNextDestination.setLayout(new GridBagLayout());
		pNextDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("NextDestination")));
		addItem(pNextDestination, textName3, 1, 0);
		addItem(pNextDestination, textTrack3, 2, 0);
		addItem(pNextDestination, nextDestinationBox, 1, 1);
		addItem(pNextDestination, nextDestTrackBox, 2, 1);
		addItem(pNextDestination, autoNextDestTrackCheckBox, 3, 1);
		pOptional2.add(pNextDestination);
		
		// row 8
		JPanel pTrain = new JPanel();
		pTrain.setLayout(new GridBagLayout());
		pTrain.setBorder(BorderFactory.createTitledBorder(rb.getString("Train")));
		addItem(pTrain, trainBox, 0, 0);
		pOptional2.add(pTrain);

		// button panel
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		addItem(pButtons, saveButton, 2, 10);
		
		// add panels
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		getContentPane().add(pPanel);
		getContentPane().add(pOptional1);
		getContentPane().add(pOptional2);
		getContentPane().add(pButtons);
		
		// select auto mode
		//autoTrackCheckBox.setSelected(true);
		//autoDestinationTrackCheckBox.setSelected(true);
		//autoNextDestTrackCheckBox.setSelected(true);
		//autoReturnWhenEmptyTrackCheckBox.setSelected(true);
		
		// setup buttons
		addButtonAction(saveButton);
		
		// setup combobox
		addComboBoxAction(locationBox);
		addComboBoxAction(destinationBox);
		addComboBoxAction(nextDestinationBox);
		addComboBoxAction(destReturnWhenEmptyBox);
		
		// setup checkbox
		addCheckBoxAction(locationUnknownCheckBox);
		addCheckBoxAction(autoTrackCheckBox);
		addCheckBoxAction(autoDestinationTrackCheckBox);
		addCheckBoxAction(autoNextDestTrackCheckBox);
		addCheckBoxAction(autoReturnWhenEmptyTrackCheckBox);
		
		// add tool tips
		autoTrackCheckBox.setToolTipText(rb.getString("TipAutoTrack"));
		autoDestinationTrackCheckBox.setToolTipText(rb.getString("TipAutoTrack"));
		autoNextDestTrackCheckBox.setToolTipText(rb.getString("TipAutoTrack"));
		autoReturnWhenEmptyTrackCheckBox.setToolTipText(rb.getString("TipAutoTrack"));

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);

		//	 get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);
		// get notified if train combo box gets modified
		trainManager.addPropertyChangeListener(this);
		
		// Only show returnWhenEmpty and nextDestination if routing enabled
		pOptional1.setVisible(Setup.isCarRoutingEnabled());
		pNextDestination.setVisible(Setup.isCarRoutingEnabled());
		
		// set frame size and location for display
		packFrame();
		setLocation(Control.panelX, Control.panelY);
		setMinimumSize(new Dimension(500, getHeight()));
	}
	
	public void loadCar(Car car){
		_car = car;
		textCarRoad.setText(car.getRoad()+" "+car.getNumber());
		textCarType.setText(car.getType());
		locationUnknownCheckBox.setSelected(car.isLocationUnknown());
		updateComboBoxes();
		enableComponents(!locationUnknownCheckBox.isSelected());
		if (_car.getRouteLocation() != null)
			log.debug("car has a pickup location "+_car.getRouteLocation().getName());
		if (_car.getRouteDestination() != null)
			log.debug("car has a destination "+_car.getRouteDestination().getName());
		// has the program generated a pickup and drop for this car?
		if (_car.getRouteLocation() != null || _car.getRouteDestination() != null){
			JOptionPane.showMessageDialog(this,
					rb.getString("pressSaveWill"),	rb.getString("carInRoute"),
					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private void updateComboBoxes(){
		log.debug("update combo boxes");
		locationManager.updateComboBox(locationBox);
		locationBox.setSelectedItem(_car.getLocation());	
		locationManager.updateComboBox(destinationBox);
		destinationBox.setSelectedItem(_car.getDestination());
		locationManager.updateComboBox(nextDestinationBox);
		nextDestinationBox.setSelectedItem(_car.getNextDestination());
		locationManager.updateComboBox(destReturnWhenEmptyBox);
		destReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestination());
		trainManager.updateComboBox(trainBox);
		trainBox.setSelectedItem(_car.getTrain());
		
		// update track combo boxes
		updateLocation();
		updateDestination();
		updateNextDestination();
		updateReturnWhenEmpty();
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== locationBox){
			updateLocation();
		}
		if (ae.getSource()== destinationBox){
			updateDestination();
		}
		if (ae.getSource()== nextDestinationBox){
			updateNextDestination();
		}
		if (ae.getSource()== destReturnWhenEmptyBox){
			updateReturnWhenEmpty();
		}
	}
	
	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {
			log.debug("Save button action");
			// save the unknown status
			_car.setLocationUnknown(locationUnknownCheckBox.isSelected());
			
			if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
				_car.setLocation(null, null);
			} else {
				if (trackLocationBox.getSelectedItem() == null
						|| trackLocationBox.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(this,
							rb.getString("carFullySelect"),	rb.getString("carCanNotLoc"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = _car.setLocation((Location) locationBox.getSelectedItem(),
						(Track)trackLocationBox.getSelectedItem());
				if (!status.equals(Car.OKAY)){
					log.debug ("Can't set car's location because of "+ status);
					JOptionPane.showMessageDialog(this,
							rb.getString("carCanNotLocMsg")+ status,
							rb.getString("carCanNotLoc"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (destinationBox.getSelectedItem() == null || destinationBox.getSelectedItem().equals("")) {
				_car.setDestination(null, null);
			} else {
				Track destTrack = null;
				if (trackDestinationBox.getSelectedItem() != null 
						&& !trackDestinationBox.getSelectedItem().equals("")){
					destTrack = (Track)trackDestinationBox.getSelectedItem();
				}
				Location destination = (Location) destinationBox.getSelectedItem();
				String status = _car.setDestination(destination, destTrack);
				if (!status.equals(Car.OKAY)){
					log.debug ("Can't set car's destination because of "+ status);
					JOptionPane.showMessageDialog(this,
							rb.getString("carCanNotDestMsg")+ status,
							rb.getString("carCanNotDest"),
							JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					if (TrainManager.instance().getTrainForCar(_car) == null){
						JOptionPane.showMessageDialog(this,
								rb.getString("carNotServByAnyTrain"),
								rb.getString("carNotMove"),
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			if (destReturnWhenEmptyBox.getSelectedItem() == null || destReturnWhenEmptyBox.getSelectedItem().equals("")) {
				_car.setReturnWhenEmptyDestination(null);
				_car.setReturnWhenEmptyDestTrack(null);
			} else {
				if (trackReturnWhenEmptyBox.getSelectedItem() != null 
						&& !trackReturnWhenEmptyBox.getSelectedItem().equals("")){
					_car.setReturnWhenEmptyDestTrack((Track)trackReturnWhenEmptyBox.getSelectedItem());
				} else {
					_car.setReturnWhenEmptyDestTrack(null);
				}
				_car.setReturnWhenEmptyDestination((Location) destReturnWhenEmptyBox.getSelectedItem());
			}
			if (nextDestinationBox.getSelectedItem() == null || nextDestinationBox.getSelectedItem().equals("")) {
				_car.setNextDestination(null);
				_car.setNextDestTrack(null);
			} else {
				if (nextDestTrackBox.getSelectedItem() != null 
						&& !nextDestTrackBox.getSelectedItem().equals("")){
					_car.setNextDestTrack((Track)nextDestTrackBox.getSelectedItem());
				} else {
					_car.setNextDestTrack(null);
				}
				_car.setNextDestination((Location) nextDestinationBox.getSelectedItem());
			}
			if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals(""))
				_car.setTrain(null);
			else {
				_car.setTrain((Train)trainBox.getSelectedItem());
				Train train = _car.getTrain();
				// determine if train services this car's type
				if (train != null && !train.acceptsTypeName(_car.getType())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("carTrainNotServType"), new Object[]{_car.getType(), train.getName()}),
							rb.getString("carNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// determine if train services this car's road
				if (train != null && !train.acceptsRoadName(_car.getRoad())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("carTrainNotServRoad"), new Object[]{_car.getRoad(), train.getName()}),
							rb.getString("carNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// determine if train services this car's built date
				if (train != null && !train.acceptsBuiltDate(_car.getBuilt())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("carTrainNotServBuilt"), new Object[]{_car.getBuilt(), train.getName()}),
							rb.getString("carNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// determine if train services this car's load
				if (train != null && !train.acceptsLoadName(_car.getLoad())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("carTrainNotServLoad"), new Object[]{_car.getLoad(), train.getName()}),
							rb.getString("carNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				// determine if train services this car's built date
				if (train != null && !train.acceptsOwnerName(_car.getOwner())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("carTrainNotServOwner"), new Object[]{_car.getOwner(), train.getName()}),
							rb.getString("carNotMove"),
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
						rl = route.getLastLocationByName(_car.getLocationName());
						rd = route.getLastLocationByName(_car.getDestinationName());
					}
				} else {
					log.error("Expected a train from combobox");
					return;
				}
				if (rl == null){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("carLocNotServ"), new Object[]{_car.getLocationName(), train.getName()}),
							rb.getString("carNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (rd == null && !_car.getDestinationName().equals("")){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("carDestNotServ"), new Object[]{_car.getDestinationName(), train.getName()}),
							rb.getString("carNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return;
				} 
				if (rd != null && route != null){
					// now determine if destination is after location
					List<String> routeSequence = route.getLocationsBySequenceList();
					boolean foundLoc = false;	// when true, found the car's location in the route
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
					if (!foundDes){
						JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carLocOrder"),
								new Object[] {_car.getDestinationName(),	_car.getLocationName(),
							train.getName() }), rb.getString("carNotMove"),
							JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (!train.servicesCar(_car)){
						JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carTrainNotService"),
								new Object[] {train.getName()}), rb.getString("carNotMove"),
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			// is this car part of a kernel?
			if (_car.getKernel() != null){
				if (JOptionPane.showConfirmDialog(this,
						rb.getString("carInKernel"),
						rb.getString("carPartKernel"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					List<Car> kCars = _car.getKernel().getCars();
					for(int i=0; i<kCars.size(); i++){
						Car kCar = kCars.get(i);
						if (kCar == _car)
							continue;
						if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
							kCar.setLocation(null, null);
						} else {
							String status = kCar.setLocation((Location) locationBox.getSelectedItem(),
									(Track)trackLocationBox.getSelectedItem());
							if (!status.equals(Car.OKAY)){
								log.debug ("Can't set the location for all of the cars in the kernel because of "+ status);
								JOptionPane.showMessageDialog(this,
										rb.getString("carCanNotLocMsg")+ status,
										rb.getString("carCanNotLoc"),
										JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						if (destinationBox.getSelectedItem() == null || destinationBox.getSelectedItem().equals("")) {
							kCar.setDestination(null, null);
						} else {
							String status = kCar.setDestination((Location) destinationBox.getSelectedItem(),
									(Track)trackDestinationBox.getSelectedItem());
							if (!status.equals(Car.OKAY)){
								log.debug ("Can't set the destination for all of the cars in the kernel because of "+ status);
								JOptionPane.showMessageDialog(this,
										rb.getString("carCanNotDestMsg")+ status,
										rb.getString("carCanNotDest"),
										JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals("")){
							kCar.setTrain(null);
						} else {
							kCar.setTrain((Train)trainBox.getSelectedItem());
						}
						// remove car from being picked up and delivered
						kCar.setRouteLocation(null);
						kCar.setRouteDestination(null);
					}
				}
			}
			// remove car from being picked up and delivered
			_car.setRouteLocation(null);
			_car.setRouteDestination(null);
			managerXml.writeOperationsFile();
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == locationUnknownCheckBox)
			enableComponents(!locationUnknownCheckBox.isSelected());
		if (ae.getSource() == autoTrackCheckBox) 
			updateLocation();
		if (ae.getSource() == autoDestinationTrackCheckBox) 
			updateDestination();
		if (ae.getSource() == autoNextDestTrackCheckBox) 
			updateNextDestination();
		if (ae.getSource() == autoReturnWhenEmptyTrackCheckBox) 
			updateReturnWhenEmpty();
	}
	
	protected void enableComponents(boolean enabled){
		// combo boxes
		locationBox.setEnabled(enabled);
		trackLocationBox.setEnabled(enabled); 
		destinationBox.setEnabled(enabled);
		trackDestinationBox.setEnabled(enabled); 
		destReturnWhenEmptyBox.setEnabled(enabled);
		trackReturnWhenEmptyBox.setEnabled(enabled); 
		nextDestinationBox.setEnabled(enabled);
		nextDestTrackBox.setEnabled(enabled);
		trainBox.setEnabled(enabled);
		// checkboxes
		autoTrackCheckBox.setEnabled(enabled);
		autoDestinationTrackCheckBox.setEnabled(enabled);
		autoNextDestTrackCheckBox.setEnabled(enabled);
		autoReturnWhenEmptyTrackCheckBox.setEnabled(enabled);
	}
	
	protected void updateLocation(){
		if (locationBox.getSelectedItem() != null){
			if (locationBox.getSelectedItem().equals("")){
				trackLocationBox.removeAllItems();
			}else{
				log.debug("CarSetFrame sees location: "+ locationBox.getSelectedItem());
				Location l = (Location)locationBox.getSelectedItem();
				l.updateComboBox(trackLocationBox);
				findAvailableTracks(l, trackLocationBox, autoTrackCheckBox.isSelected(), false);
				if (_car.getLocation() != null && _car.getLocation().equals(l) && _car.getTrack() != null)
					trackLocationBox.setSelectedItem(_car.getTrack());
				packFrame();
			}
		}
	}
	
	protected void updateDestination(){
		if (destinationBox.getSelectedItem() != null){
			if (destinationBox.getSelectedItem().equals("")){
				trackDestinationBox.removeAllItems();
			}else{
				log.debug("CarSetFrame sees destination: "+ destinationBox.getSelectedItem());
				Location l = (Location)destinationBox.getSelectedItem();
				l.updateComboBox(trackDestinationBox);
				findAvailableTracks(l, trackDestinationBox, autoDestinationTrackCheckBox.isSelected(), true);
				if (_car.getDestination() != null && _car.getDestination().equals(l) && _car.getDestinationTrack() != null)
					trackDestinationBox.setSelectedItem(_car.getDestinationTrack());
				packFrame();
			}
		}
	}
	
	protected void updateReturnWhenEmpty(){
		if (destReturnWhenEmptyBox.getSelectedItem() != null){
			if (destReturnWhenEmptyBox.getSelectedItem().equals("")){
				trackReturnWhenEmptyBox.removeAllItems();
			}else{
				log.debug("CarSetFrame sees return when empty: "+ destReturnWhenEmptyBox.getSelectedItem());
				Location l = (Location)destReturnWhenEmptyBox.getSelectedItem();
				l.updateComboBox(trackReturnWhenEmptyBox);
				findAvailableTracks(l, trackReturnWhenEmptyBox, autoReturnWhenEmptyTrackCheckBox.isSelected(), true);
				if (_car.getReturnWhenEmptyDestination() != null && _car.getReturnWhenEmptyDestination().equals(l) && _car.getReturnWhenEmptyDestTrack() != null)
					trackReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestTrack());
				packFrame();
			}
		}
	}
	
	protected void updateNextDestination(){
		if (nextDestinationBox.getSelectedItem() != null){
			if (nextDestinationBox.getSelectedItem().equals("")){
				nextDestTrackBox.removeAllItems();
			}else{
				log.debug("CarSetFrame sees destination: "+ nextDestinationBox.getSelectedItem());
				Location l = (Location)nextDestinationBox.getSelectedItem();
				l.updateComboBox(nextDestTrackBox);
				findAvailableTracks(l, nextDestTrackBox, autoNextDestTrackCheckBox.isSelected(), true);
				if (_car.getNextDestination() != null && _car.getNextDestination().equals(l) && _car.getNextDestTrack() != null)
					nextDestTrackBox.setSelectedItem(_car.getNextDestTrack());
				packFrame();
			}
		}
	}
	
	/**
	 * Find the available tracks that will accept this car
	 * @param l location
	 */
	protected void findAvailableTracks(Location l, JComboBox box, boolean filter, boolean destTrack){
		if(filter && l != null){
			List<String> tracks = l.getTracksByNameList(null);
			for (int i=0; i<tracks.size(); i++){
				Track track = l.getTrackById(tracks.get(i));
				String status = "";
				if (destTrack){
					status = _car.testDestination(l, track);
				} else {
					status = _car.testLocation(l, track);
				}
				if (status.equals(Car.OKAY) && (!destTrack || !track.getLocType().equals(Track.STAGING))){
					box.setSelectedItem(track);
					log.debug("Available track: "+track.getName()+" for location: "+l.getName());
				} else {
					box.removeItem(track);
				}
			}
		}
	}
	
	protected void packFrame(){
		pack();
		if ((getWidth()<450)) 
			setSize(500, getHeight()+50);
		else
			setSize (getWidth()+50, getHeight()+50);		
		setVisible(true);
	}

	public void dispose(){
		LocationManager.instance().removePropertyChangeListener(this);
		trainManager.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("CarSetFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)){
			updateComboBoxes();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarSetFrame.class.getName());
}
