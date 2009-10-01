// CarSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user to place car on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.8 $
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
		JPanel pCar = new JPanel();
		pCar.setLayout(new GridBagLayout());
		pCar.setBorder(BorderFactory.createTitledBorder(rb.getString("Car")));
		addItem(pCar, textCarRoad, 1, 0);
		pPanel.add(pCar);
		
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
		pOptional.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptional")));

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
		addHelpMenu("package.jmri.jmrit.operations.Operations_Cars", true);

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
	
	public void loadCar(Car car){
		_car = car;
		textCarRoad.setText(car.getRoad()+" "+car.getNumber());
		updateComboBoxes();
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
		Location l = _car.getLocation();
		if (l != null){
			l.updateComboBox(trackLocationBox);
			trackLocationBox.setSelectedItem(_car.getTrack());
		}		
		locationManager.updateComboBox(destinationBox);
		destinationBox.setSelectedItem(_car.getDestination());
		l = _car.getDestination();
		if (l != null){
			l.updateComboBox(trackDestinationBox);
			trackDestinationBox.setSelectedItem(_car.getDestinationTrack());
		}
		trainManager.updateComboBox(trainBox);
		trainBox.setSelectedItem(_car.getTrain());
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== locationBox){
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")){
					trackLocationBox.removeAllItems();
				}else{
					log.debug("CarSetFrame sees location: "+ locationBox.getSelectedItem());
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
					log.debug("CarSetFrame sees destination: "+ destinationBox.getSelectedItem());
					Location l = (Location)destinationBox.getSelectedItem();
					l.updateComboBox(trackDestinationBox);
				}
			}
		}
	}
	
	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {
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
				if (trackDestinationBox.getSelectedItem() == null
						|| trackDestinationBox.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(this,
							rb.getString("carFullyDest"),
							rb.getString("carCanNotDest"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = _car.setDestination((Location) destinationBox.getSelectedItem(),
						(Track)trackDestinationBox.getSelectedItem());
				if (!status.equals(Car.OKAY)){
					log.debug ("Can't set car's destination because of "+ status);
					JOptionPane.showMessageDialog(this,
							rb.getString("carCanNotLocMsg")+ status,
							rb.getString("carCanNotDest"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals(""))
				_car.setTrain(null);
			else {
				_car.setTrain((Train)trainBox.getSelectedItem());
				Train train = _car.getTrain();
				// determine if train services this car's type
				if (train != null && !train.acceptsTypeName(_car.getType())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("carTrainNotServ"), new Object[]{_car.getType(), train.getName()}),
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
			managerXml.writeOperationsCarFile();
		}
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
