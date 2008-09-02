// CarsSetFrame.java

package jmri.jmrit.operations.cars;

import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.SecondaryLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.OperationsFrame;


import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Frame for user to place car on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.2 $
 */

public class CarsSetFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();
	TrainManager trainManager = TrainManager.instance();
	
	Car _car;
		
	// labels
	javax.swing.JLabel textCar = new javax.swing.JLabel();
	javax.swing.JLabel textCarRoad = new javax.swing.JLabel();
	javax.swing.JLabel textName = new javax.swing.JLabel();
	javax.swing.JLabel textSecondary = new javax.swing.JLabel();
	javax.swing.JLabel textLocation = new javax.swing.JLabel();
	javax.swing.JLabel textOptional = new javax.swing.JLabel();
	javax.swing.JLabel textDestination = new javax.swing.JLabel();
	javax.swing.JLabel textTrain = new javax.swing.JLabel();

	// major buttons
	
	javax.swing.JButton saveButton = new javax.swing.JButton();

	// for padding out panel
	javax.swing.JLabel space1 = new javax.swing.JLabel();
	javax.swing.JLabel space2 = new javax.swing.JLabel();
	javax.swing.JLabel space3 = new javax.swing.JLabel();
	
	// combo boxes
	javax.swing.JComboBox locationBox = LocationManager.instance().getComboBox();
	javax.swing.JComboBox secondaryLocationBox = new javax.swing.JComboBox(); 
	javax.swing.JComboBox destinationBox = LocationManager.instance().getComboBox();
	javax.swing.JComboBox secondaryDestinationBox = new javax.swing.JComboBox(); 
	javax.swing.JComboBox trainBox = TrainManager.instance().getComboBox();
		
	public CarsSetFrame() {
		super();
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state

		textCar.setText(rb.getString("Car"));
		textCar.setVisible(true);
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);
		textSecondary.setText(rb.getString("Secondary"));
		textSecondary.setVisible(true);
		textLocation.setText(rb.getString("Location"));
		textLocation.setVisible(true);
		textOptional.setText("-------------------------------- Optional ------------------------------------");
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
		addItem(textSecondary, 2, 2);
		
		// row 3
		addItem(textLocation, 0, 3);
		addItem(locationBox, 1, 3);
		addItem(secondaryLocationBox, 2, 3);
		
		// row 4
		addItemWidth(textOptional, 3, 0, 4);

		// row 6
		addItem(textDestination, 0, 6);
		addItem(destinationBox, 1, 6);
		addItem(secondaryDestinationBox, 2, 6);

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
		JMenuBar menuBar = new JMenuBar();
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
			l.updateComboBox(secondaryLocationBox);
			secondaryLocationBox.setSelectedItem(_car.getSecondaryLocation());
		}		
		locationManager.updateComboBox(destinationBox);
		destinationBox.setSelectedItem(_car.getDestination());
		l = _car.getDestination();
		if (l != null){
			l.updateComboBox(secondaryDestinationBox);
			secondaryDestinationBox.setSelectedItem(_car.getSecondaryDestination());
		}
		trainManager.updateComboBox(trainBox);
		trainBox.setSelectedItem(_car.getTrain());
	}
	
	private void addComboBoxAction(JComboBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				comboBoxActionPerformed(e);
			}
		});
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("CarsSetFrame sees location: "+ ae.getSource());
		if (ae.getSource()== locationBox){
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")){
					secondaryLocationBox.removeAllItems();
				}else{
					log.debug("CarsSetFrame sees location: "+ locationBox.getSelectedItem());
					Location l = (Location)locationBox.getSelectedItem();
					l.updateComboBox(secondaryLocationBox);
				}
			}
		}
		if (ae.getSource()== destinationBox){
			if (destinationBox.getSelectedItem() != null){
				if (destinationBox.getSelectedItem().equals("")){
					secondaryDestinationBox.removeAllItems();
				}else{
					log.debug("CarsSetFrame sees destination: "+ destinationBox.getSelectedItem());
					Location l = (Location)destinationBox.getSelectedItem();
					l.updateComboBox(secondaryDestinationBox);
				}
			}
		}
	}
	
	private void addButtonAction(JButton b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				buttonActionPerformed(e);
			}
		});
	}
	
	// Save, Delete, Add, Clear, Calculate buttons
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {
			if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
				_car.setLocation(null, null);
			} else {
				if (secondaryLocationBox.getSelectedItem() == null
						|| secondaryLocationBox.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(this,
							"Must fully select a car's location",
							"Can not update car location",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = _car.setLocation((Location) locationBox.getSelectedItem(),
						(SecondaryLocation)secondaryLocationBox.getSelectedItem());
				if (!status.equals(Car.OKAY)){
					log.debug ("Can't set car's location because of "+ status);
					JOptionPane.showMessageDialog(this,
							"Can't set car's location because of location's "+ status,
							"Can not update car location",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (destinationBox.getSelectedItem() == null || destinationBox.getSelectedItem().equals("")) {
				_car.setDestination(null, null);
			} else {
				if (secondaryDestinationBox.getSelectedItem() == null
						|| secondaryDestinationBox.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(this,
							"Must fully select a car's destination",
							"Can not update car destination",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = _car.setDestination((Location) destinationBox.getSelectedItem(),
						(SecondaryLocation)secondaryDestinationBox.getSelectedItem());
				if (!status.equals(Car.OKAY)){
					log.debug ("Can't set car's destination because of "+ status);
					JOptionPane.showMessageDialog(this,
							"Can't set car's destination because of destination's "+ status,
							"Can not update car destination",
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
							"Car's location ("+_car.getLocationName()+") not serviced by train "+ train.getName(),
							"Car will not move!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (rd == null && !_car.getDestinationName().equals("")){
					JOptionPane.showMessageDialog(this,
							"Car's destination ("+_car.getDestinationName()+") not serviced by train "+ train.getName(),
							"Car will not move!",
							JOptionPane.ERROR_MESSAGE);
					return;
				} 
				if (rd != null){
					// now determine if destination is after location
					List routeSequence = route.getLocationsBySequenceList();
					boolean foundLoc = false;	// when true, found the car's location in the route
					boolean foundDes = false;
					for (int i=0; i<routeSequence.size(); i++){
						String locId = (String)routeSequence.get(i);
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
						JOptionPane.showMessageDialog(this,
								"Car's destination ("+_car.getDestinationName()+") is before location ("+_car.getLocationName()+") when serviced by train "+ train.getName(),
								"Car will not move!",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			managerXml.writeOperationsCarFile();
			trainManagerXml.writeOperationsTrainFile();
		}
	}

	public void dispose(){
		// LocationManager.instance().removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("CarsSetFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH)){
			updateComboBoxes();
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(CarsSetFrame.class.getName());
}
