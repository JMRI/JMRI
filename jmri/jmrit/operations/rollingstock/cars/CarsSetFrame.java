// CarsSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JLabel;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;


/**
 * Frame for user to place car on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.10 $
 */

public class CarsSetFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();
	TrainManager trainManager = TrainManager.instance();
	
	Car _car;
		
	// labels
	JLabel textCar = new JLabel();
	JLabel textCarRoad = new JLabel();
	JLabel textName = new JLabel();
	JLabel textTrack = new JLabel();
	JLabel textLocation = new JLabel();
	JLabel textOptional = new JLabel();
	JLabel textDestination = new JLabel();
	JLabel textTrain = new JLabel();

	// major buttons
	
	JButton saveButton = new JButton();

	// for padding out panel
	JLabel space1 = new JLabel();
	JLabel space2 = new JLabel();
	JLabel space3 = new JLabel();
	
	// combo boxes
	JComboBox locationBox = LocationManager.instance().getComboBox();
	JComboBox trackLocationBox = new JComboBox(); 
	JComboBox destinationBox = LocationManager.instance().getComboBox();
	JComboBox trackDestinationBox = new JComboBox(); 
	JComboBox trainBox = TrainManager.instance().getComboBox();
		
	public CarsSetFrame() {
		super();
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state

		textCar.setText(rb.getString("Car"));
		textCar.setVisible(true);
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);
		textTrack.setText(rb.getString("Track"));
		textTrack.setVisible(true);
		textLocation.setText(rb.getString("Location"));
		textLocation.setVisible(true);
		textOptional.setText(rb.getString("Optional"));
		textOptional.setVisible(true);
		textDestination.setText(rb.getString("Destination"));
		textDestination.setVisible(true);
		textTrain.setText(rb.getString("Train"));
		textTrain.setVisible(true);
		
		saveButton.setText(rb.getString("Save"));
		saveButton.setVisible(true);
		
		getContentPane().setLayout(new GridBagLayout());
		
		// Layout the panel by rows
		// row 1
		addItem(textCar, 0, 1);
		addItemLeft(textCarRoad, 1, 1);
		
		// row 2
		addItem(textName, 1, 2);
		addItem(textTrack, 2, 2);
		
		// row 3
		addItem(textLocation, 0, 3);
		addItem(locationBox, 1, 3);
		addItem(trackLocationBox, 2, 3);
		
		// row 4
		addItemWidth(textOptional, 3, 0, 4);

		// row 6
		addItem(textDestination, 0, 6);
		addItem(destinationBox, 1, 6);
		addItem(trackDestinationBox, 2, 6);

		// row 8
		addItem(textTrain, 0, 8);
		addItem(trainBox, 1, 8);

		// row 10
		addItem(saveButton, 2, 10);
		
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
			setSize(450, getHeight()+20);
		else
			setSize (getWidth()+50, getHeight()+20);
		setLocation(500, 500);
// 		setAlwaysOnTop(true);	// this blows up in Java 1.4 
		setVisible(true);
	}
	
	public void loadCar(Car car){
		_car = car;
		textCarRoad.setText(car.getRoad()+" "+car.getNumber());
		updateComboBoxes();

		trainBox.setSelectedItem(car.getTrain());
	}
	
	private void updateComboBoxes(){
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
					log.debug("CarsSetFrame sees location: "+ locationBox.getSelectedItem());
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
					log.debug("CarsSetFrame sees destination: "+ destinationBox.getSelectedItem());
					Location l = (Location)destinationBox.getSelectedItem();
					l.updateComboBox(trackDestinationBox);
				}
			}
		}
	}
	
	// Save, Delete, Add, Clear, Calculate buttons
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
				// determine if train services the location and destination selected by user
				Train train = _car.getTrain();
				RouteLocation rl = null;
				RouteLocation rd = null;
				Route route = null;
				if (train != null){
					route = train.getRoute();
					if (route != null){
						rl = route.getLocationByName(_car.getLocationName());
						rd = route.getLocationByName(_car.getDestinationName());
					}
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
				if (rd != null){
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
					}
				}
			}
			managerXml.writeOperationsCarFile();
			trainManagerXml.writeOperationsTrainFile();
		}
	}

	public void dispose(){
		LocationManager.instance().removePropertyChangeListener(this);
		trainManager.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("CarsSetFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)){
			updateComboBoxes();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarsSetFrame.class.getName());
}
