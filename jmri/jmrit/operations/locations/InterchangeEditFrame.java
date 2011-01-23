// InterchangeEditFrame.java

package jmri.jmrit.operations.locations;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user edit of an interchange track.  Adds two panels 
 * to TrackEditFram for train/route car drops and pickups.
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.23 $
 */

public class InterchangeEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// there are two managers 
	TrainManager trainManager = TrainManager.instance();
	RouteManager routeManager = RouteManager.instance();
	
	// labels, buttons, etc. for interchanges
	//JLabel textDrops = new JLabel(rb.getString("TrainsOrRoutesDrops"));
	//JLabel textPickups = new JLabel(rb.getString("TrainsOrRoutesPickups"));
	
	JButton deleteDropButton = new JButton(rb.getString("Delete"));
	JButton addDropButton = new JButton(rb.getString("Add"));
	JButton deletePickupButton = new JButton(rb.getString("Delete"));
	JButton addPickupButton = new JButton(rb.getString("Add"));
	
	JRadioButton anyDrops = new JRadioButton(rb.getString("Any"));
	JRadioButton trainDrop = new JRadioButton(rb.getString("Trains"));
	JRadioButton routeDrop = new JRadioButton(rb.getString("Routes"));
	ButtonGroup dropGroup = new ButtonGroup();
	
	JRadioButton anyPickups = new JRadioButton(rb.getString("Any"));
	JRadioButton trainPickup = new JRadioButton(rb.getString("Trains"));
	JRadioButton routePickup = new JRadioButton(rb.getString("Routes"));
	ButtonGroup pickupGroup = new ButtonGroup();
	
	JComboBox comboBoxDropTrains = trainManager.getComboBox();
	JComboBox comboBoxDropRoutes = routeManager.getComboBox();
	JComboBox comboBoxPickupTrains = trainManager.getComboBox();
	JComboBox comboBoxPickupRoutes = routeManager.getComboBox();
	
	JPanel dropPanel = panelOpt1;
	JPanel pickupPanel = panelOpt2;
	
	public InterchangeEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.INTERCHANGE;
		
		// load the two option panels
		// drop panel
		dropPanel.setLayout(new GridBagLayout());
		dropPanel.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainsOrRoutesDrops")));
		dropPanel.add(anyDrops);
		dropPanel.add(trainDrop);
		dropPanel.add(routeDrop);

		
		// pickup panel
		pickupPanel.setLayout(new GridBagLayout());
		pickupPanel.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainsOrRoutesPickups")));
		pickupPanel.add(anyPickups);
		pickupPanel.add(trainPickup);
		pickupPanel.add(routePickup);

		
		super.initComponents(location, track);
		
		_toolMenu.add(new ChangeTrackTypeAction (this));
		addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true);
		
		// override text strings for tracks
		panelTrainDir.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainInterchange")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(rb.getString("TypesInterchange")));
		deleteTrackButton.setText(rb.getString("DeleteInterchange"));
		addTrackButton.setText(rb.getString("AddInterchange"));
		saveTrackButton.setText(rb.getString("SaveInterchange"));
		
		// setup the pickup and drop panels
		updateDropOptions();
		updatePickupOptions();
		
		// setup buttons
		addButtonAction(deleteDropButton);
		addButtonAction(addDropButton);
		addButtonAction(deletePickupButton);
		addButtonAction(addPickupButton);
		
		addRadioButtonAction(anyDrops);
		addRadioButtonAction(trainDrop);
		addRadioButtonAction(routeDrop);
		addRadioButtonAction(anyPickups);
		addRadioButtonAction(trainPickup);
		addRadioButtonAction(routePickup);

		// add property change
		trainManager.addPropertyChangeListener(this);
		routeManager.addPropertyChangeListener(this);
		
		// finish
		packFrame();
		setVisible(true);
	}
	
	protected void enableButtons(boolean enabled){
		anyDrops.setEnabled(enabled);
		trainDrop.setEnabled(enabled);
		routeDrop.setEnabled(enabled);
		anyPickups.setEnabled(enabled);
		trainPickup.setEnabled(enabled);
		routePickup.setEnabled(enabled);
		super.enableButtons(enabled);
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae){
		if (ae.getSource() == addDropButton){
			String id ="";
			if (trainDrop.isSelected()){
				if (comboBoxDropTrains.getSelectedItem().equals(""))
					return;
				Train train = ((Train) comboBoxDropTrains.getSelectedItem());
				Route route = train.getRoute();
				id = train.getId();
				if (!checkRoute(route)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("TrackNotByTrain"),new Object[]{train.getName()}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				if (comboBoxDropRoutes.getSelectedItem().equals(""))
					return;
				Route route = ((Route) comboBoxDropRoutes.getSelectedItem());
				id = route.getId();
				if (!checkRoute(route)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("TrackNotByRoute"),new Object[]{route.getName()}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			_track.addDropId(id);
			updateDropOptions();
		}
		if (ae.getSource() == deleteDropButton){
			String id ="";
			if (trainDrop.isSelected()){
				if (comboBoxDropTrains.getSelectedItem().equals(""))
					return;
				id = ((Train) comboBoxDropTrains.getSelectedItem()).getId();
			} else{
				if (comboBoxDropRoutes.getSelectedItem().equals(""))
					return;
				id = ((Route) comboBoxDropRoutes.getSelectedItem()).getId();
			}
			_track.deleteDropId(id);
			updateDropOptions();
		}
		if (ae.getSource() == addPickupButton){
			String id ="";
			if (trainPickup.isSelected()){
				if (comboBoxPickupTrains.getSelectedItem().equals(""))
					return;
				Train train = ((Train) comboBoxPickupTrains.getSelectedItem());
				Route route = train.getRoute();
				id = train.getId();
				if (!checkRoute(route)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("TrackNotByTrain"),new Object[]{train.getName()}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else{
				if (comboBoxPickupRoutes.getSelectedItem().equals(""))
					return;
				Route route = ((Route) comboBoxPickupRoutes.getSelectedItem());
				id = route.getId();
				if (!checkRoute(route)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(rb.getString("TrackNotByRoute"),new Object[]{route.getName()}), rb.getString("Error"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			_track.addPickupId(id);
			updatePickupOptions();
		}
		if (ae.getSource() == deletePickupButton){
			String id ="";
			if (trainPickup.isSelected()){
				if (comboBoxPickupTrains.getSelectedItem().equals(""))
					return;
				id = ((Train) comboBoxPickupTrains.getSelectedItem()).getId();
			} else{
				if (comboBoxPickupRoutes.getSelectedItem().equals(""))
					return;
				id = ((Route) comboBoxPickupRoutes.getSelectedItem()).getId();
			}
			_track.deletePickupId(id);
			updatePickupOptions();
		}
		super.buttonActionPerformed(ae);
	}
	
	// check to see if the route services this location
	private boolean checkRoute (Route route){
		if (route == null)
			return false;
		RouteLocation rl = null;
		rl = route.getLastLocationByName(_location.getName());
		if (rl == null)
			return false;
		return true;
	}
	
	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == anyDrops){
			_track.setDropOption(Track.ANY);
			updateDropOptions();
		}
		if (ae.getSource() == trainDrop){
			_track.setDropOption(Track.TRAINS);
			updateDropOptions();
		}
		if (ae.getSource() == routeDrop){
			_track.setDropOption(Track.ROUTES);
			updateDropOptions();
		}
		if (ae.getSource() == anyPickups){
			_track.setPickupOption(Track.ANY);
			updatePickupOptions();
		}
		if (ae.getSource() == trainPickup){
			_track.setPickupOption(Track.TRAINS);
			updatePickupOptions();
		}
		if (ae.getSource() == routePickup){
			_track.setPickupOption(Track.ROUTES);
			updatePickupOptions();
		}
		super.radioButtonActionPerformed(ae);
	}
	
	//TODO only update comboBox when train or route list changes. 
	private void updateDropOptions(){
		dropPanel.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(anyDrops, 0);
    	p.add(trainDrop, 1);
    	p.add(routeDrop, 2);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	dropPanel.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_track != null){
			// set radio button
			anyDrops.setSelected(_track.getDropOption().equals(Track.ANY));
			trainDrop.setSelected(_track.getDropOption().equals(Track.TRAINS));
			routeDrop.setSelected(_track.getDropOption().equals(Track.ROUTES));
			
			if (!anyDrops.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	if (trainDrop.isSelected()){
		    		p.add(comboBoxDropTrains);
		    	} else {
		    		p.add(comboBoxDropRoutes);
		    	}
		    	p.add(addDropButton);
		    	p.add(deleteDropButton);
				gc.gridy = y++;
				dropPanel.add(p, gc);
		    	y++;

		    	String[]dropIds = _track.getDropIds();
		    	int x = 0;
		    	for (int i =0; i<dropIds.length; i++){
		    		JLabel names = new JLabel();
		    		String name = "<deleted>";
		    		if (trainDrop.isSelected()){
		    			Train train = trainManager.getTrainById(dropIds[i]);
		    			if(train != null)
		    				name = train.getName();
		    		} else {
		    			Route route = routeManager.getRouteById(dropIds[i]);
		    			if(route != null)
		    				name = route.getName();
		    		}
		    		if (name.equals("<deleted>"))
		    			_track.deleteDropId(dropIds[i]);
		    		names.setText(name);
		    		addItem(dropPanel, names, x++, y);
		    		if (x > 5){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			anyDrops.setSelected(true);
		}
		dropPanel.revalidate();
		dropPanel.repaint();
		packFrame();
	}
	
	private void updatePickupOptions(){
		log.debug("update pickup options");
		pickupPanel.removeAll();
		
    	JPanel p = new JPanel();
    	p.setLayout(new GridBagLayout());
    	p.add(anyPickups, 0);
    	p.add(trainPickup, 1);
    	p.add(routePickup, 2);
    	GridBagConstraints gc = new GridBagConstraints();
    	gc.gridwidth = 6;
    	pickupPanel.add(p, gc);
		
		int y = 1;		// vertical position in panel

		if(_track != null){
			// set radio button
			anyPickups.setSelected(_track.getPickupOption().equals(Track.ANY));
			trainPickup.setSelected(_track.getPickupOption().equals(Track.TRAINS));
			routePickup.setSelected(_track.getPickupOption().equals(Track.ROUTES));
			
			if (!anyPickups.isSelected()){
		    	p = new JPanel();
		    	p.setLayout(new FlowLayout());
		    	if (trainPickup.isSelected()){
		    		p.add(comboBoxPickupTrains);
		    	} else {
		    		p.add(comboBoxPickupRoutes);
		    	}
		    	p.add(addPickupButton);
		    	p.add(deletePickupButton);
				gc.gridy = y++;
				pickupPanel.add(p, gc);
		    	y++;

		    	String[]pickupIds = _track.getPickupIds();
		    	int x = 0;
		    	for (int i =0; i<pickupIds.length; i++){
		    		JLabel names = new JLabel();
		    		String name = "<deleted>";
		    		if (trainPickup.isSelected()){
		    			Train train = trainManager.getTrainById(pickupIds[i]);
		    			if(train != null)
		    				name = train.getName();
		    		} else {
		    			Route route = routeManager.getRouteById(pickupIds[i]);
		    			if(route != null)
		    				name = route.getName();
		    		}
		    		if (name.equals("<deleted>"))
		    			_track.deletePickupId(pickupIds[i]);
		    		names.setText(name);
		    		addItem(pickupPanel, names, x++, y);
		    		if (x > 5){
		    			y++;
		    			x = 0;
		    		}
		    	}
			}
		} else {
			anyPickups.setSelected(true);
		}
		pickupPanel.revalidate();
		pickupPanel.repaint();
		packFrame();
	}
	
	private void updateTrainComboBox(){
		trainManager.updateComboBox(comboBoxPickupTrains);
		trainManager.updateComboBox(comboBoxDropTrains);
	}
	
	private void updateRouteComboBox(){
		routeManager.updateComboBox(comboBoxPickupRoutes);
		routeManager.updateComboBox(comboBoxDropRoutes);
	}
	
	public void dispose() {
		trainManager.removePropertyChangeListener(this);
		routeManager.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled()) 
			log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
		if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)){
			updateTrainComboBox();
			updateDropOptions();
			updatePickupOptions();
		}
		if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY)){
			updateRouteComboBox();
			updateDropOptions();
			updatePickupOptions();
		}
		super.propertyChange(e);
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(InterchangeEditFrame.class.getName());
}
