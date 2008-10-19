// EnginesSetFrame.java

package jmri.jmrit.operations.rollingstock.engines;

import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.rollingstock.cars.Car;
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
 * Frame for user to place engine on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.2 $
 */

public class EnginesSetFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	EngineManager manager = EngineManager.instance();
	EngineManagerXml managerXml = EngineManagerXml.instance();
	LocationManager locationManager = LocationManager.instance();
	TrainManagerXml trainManagerXml = TrainManagerXml.instance();
	TrainManager trainManager = TrainManager.instance();
	
	Engine _engine;
		
	// labels
	javax.swing.JLabel textEngine = new javax.swing.JLabel();
	javax.swing.JLabel textEngineRoad = new javax.swing.JLabel();
	javax.swing.JLabel textName = new javax.swing.JLabel();
	javax.swing.JLabel textTrack = new javax.swing.JLabel();
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
	javax.swing.JComboBox trackLocationBox = new javax.swing.JComboBox(); 
	javax.swing.JComboBox destinationBox = LocationManager.instance().getComboBox();
	javax.swing.JComboBox trackDestinationBox = new javax.swing.JComboBox(); 
	javax.swing.JComboBox trainBox = TrainManager.instance().getComboBox();
		
	public EnginesSetFrame() {
		super();
	}

	public void initComponents() {
		
		// the following code sets the frame's initial state

		textEngine.setText(rb.getString("Engine"));
		textEngine.setVisible(true);
		textName.setText(rb.getString("Name"));
		textName.setVisible(true);
		textTrack.setText(rb.getString("Track"));
		textTrack.setVisible(true);
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
		addItem(textEngine, 0, 1);
		addItemLeft(textEngineRoad, 1, 1);
		
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
		JMenuBar menuBar = new JMenuBar();
		addHelpMenu("package.jmri.jmrit.operations.Operations_Engines", true);

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
	
	public void loadEngine(Engine engine){
		_engine = engine;
		textEngineRoad.setText(engine.getRoad()+" "+engine.getNumber());
		updateComboBoxes();

		trainBox.setSelectedItem(engine.getTrain());
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
	
	private void addComboBoxAction(JComboBox b) {
		b.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				comboBoxActionPerformed(e);
			}
		});
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== locationBox){
			if (locationBox.getSelectedItem() != null){
				if (locationBox.getSelectedItem().equals("")){
					trackLocationBox.removeAllItems();
				}else{
					log.debug("EnginesSetFrame sees location: "+ locationBox.getSelectedItem());
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
					log.debug("EnginesSetFrame sees destination: "+ destinationBox.getSelectedItem());
					Location l = (Location)destinationBox.getSelectedItem();
					l.updateComboBox(trackDestinationBox);
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
				_engine.setLocation(null, null);
			} else {
				if (trackLocationBox.getSelectedItem() == null
						|| trackLocationBox.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(this,
							"Must fully select a engine's location",
							"Can not update engine location",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = _engine.setLocation((Location) locationBox.getSelectedItem(),
						(Track)trackLocationBox.getSelectedItem());
				if (!status.equals(Engine.OKAY)){
					log.debug ("Can't set engine's location because of "+ status);
					JOptionPane.showMessageDialog(this,
							"Can't set engine's location because of location's "+ status,
							"Can not update engine location",
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
							"Must fully select a engine's destination",
							"Can not update engine destination",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				String status = _engine.setDestination((Location) destinationBox.getSelectedItem(),
						(Track)trackDestinationBox.getSelectedItem());
				if (!status.equals(Engine.OKAY)){
					log.debug ("Can't set engine's destination because of "+ status);
					JOptionPane.showMessageDialog(this,
							"Can't set engine's destination because of destination's "+ status,
							"Can not update engine destination",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals(""))
				_engine.setTrain(null);
			else {
				_engine.setTrain((Train)trainBox.getSelectedItem());
				// determine if train services the location and destination selected by user
				Train train = _engine.getTrain();
				RouteLocation rl = null;
				RouteLocation rd = null;
				Route route = null;
				if (train != null){
					route = train.getRoute();
					if (route != null){
						rl = route.getLocationByName(_engine.getLocationName());
						rd = route.getLocationByName(_engine.getDestinationName());
					}
				}
				if (rl == null){
					JOptionPane.showMessageDialog(this,
							"Engine's location ("+_engine.getLocationName()+") not serviced by train "+ train.getName(),
							"Engine will not move!",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (rd == null && !_engine.getDestinationName().equals("")){
					JOptionPane.showMessageDialog(this,
							"Engine's destination ("+_engine.getDestinationName()+") not serviced by train "+ train.getName(),
							"Engine will not move!",
							JOptionPane.ERROR_MESSAGE);
					return;
				} 
				if (rd != null){
					// now determine if destination is after location
					List routeSequence = route.getLocationsBySequenceList();
					boolean foundLoc = false;	// when true, found the engine's location in the route
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
								"Engine's destination ("+_engine.getDestinationName()+") is before location ("+_engine.getLocationName()+") when serviced by train "+ train.getName(),
								"Engine will not move!",
								JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			// is this engine part of a consist?
			if (_engine.getConsist() != null){
				if (JOptionPane.showConfirmDialog(this,
						"This engine is part of a consist, do you want the other engines to also have the same settings?",
						"Engine is part of a consist",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					List engines = _engine.getConsist().getEngines();
					for(int i=0; i<engines.size(); i++){
						Engine engine = (Engine)engines.get(i);
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
										"Can't set the location for all of the engines in the consist because of "+ status,
										"Can not update engine location",
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
										"Can't set the destination for all of the engines in the consist because of "+ status,
										"Can not update engine destination",
										JOptionPane.ERROR_MESSAGE);
								return;
							}
						}
						if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals("")){
							engine.setTrain(null);
						} else {
							engine.setTrain((Train)trainBox.getSelectedItem());
						}
					}
				}
			}
			managerXml.writeOperationsEngineFile();
			trainManagerXml.writeOperationsTrainFile();
		}
	}

	public void dispose(){
		LocationManager.instance().removePropertyChangeListener(this);
		trainManager.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("EnginesSetFrame sees propertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH)){
			updateComboBoxes();
		}
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(EnginesSetFrame.class.getName());
}
