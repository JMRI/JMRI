// RollingStockSetFrame.java

package jmri.jmrit.operations.rollingstock;

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
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user to place RollingStock on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2010
 * @version $Revision: 1.7 $
 */

public class RollingStockSetFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	protected static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	RollingStockManager manager;

	protected LocationManager locationManager = LocationManager.instance();
	protected TrainManager trainManager = TrainManager.instance();
	
	RollingStock _rs;
	protected boolean _disableComboBoxUpdate = false;
		
	// labels
	JLabel textRoad = new JLabel();
	JLabel textType = new JLabel();
	JLabel textName = new JLabel(rb.getString("Name"));
	JLabel textTrack = new JLabel(rb.getString("Track"));
	JLabel textName2 = new JLabel(rb.getString("Name"));
	JLabel textTrack2 = new JLabel(rb.getString("Track"));
	JLabel textName3 = new JLabel(rb.getString("Name"));
	JLabel textTrack3 = new JLabel(rb.getString("Track"));
	JLabel textName4 = new JLabel(rb.getString("Name"));
	JLabel textTrack4 = new JLabel(rb.getString("Track"));

	// major buttons
	protected JButton saveButton = new JButton(rb.getString("Save"));
	
	// combo boxes
	protected JComboBox locationBox = LocationManager.instance().getComboBox();
	protected JComboBox trackLocationBox = new JComboBox(); 
	protected JComboBox destinationBox = LocationManager.instance().getComboBox();
	protected JComboBox trackDestinationBox = new JComboBox(); 
	protected JComboBox destReturnWhenEmptyBox = LocationManager.instance().getComboBox();
	protected JComboBox trackReturnWhenEmptyBox = new JComboBox(); 
	protected JComboBox finalDestinationBox = LocationManager.instance().getComboBox();
	protected JComboBox finalDestTrackBox = new JComboBox(); 
	protected JComboBox trainBox = TrainManager.instance().getComboBox();
	
	// check boxes
	protected JCheckBox autoTrackCheckBox = new JCheckBox(rb.getString("Auto"));
	protected JCheckBox autoDestinationTrackCheckBox = new JCheckBox(rb.getString("Auto"));
	protected JCheckBox autoFinalDestTrackCheckBox = new JCheckBox(rb.getString("Auto"));
	protected JCheckBox autoReturnWhenEmptyTrackCheckBox = new JCheckBox(rb.getString("Auto"));
	protected JCheckBox locationUnknownCheckBox = new JCheckBox(rb.getString("LocationUnknown"));
	protected JCheckBox outOfServiceCheckBox = new JCheckBox(rb.getString("OutOfService"));
	
	// optional panels
	protected JPanel pOptionalrwe = new JPanel();
	protected JPanel pFinalDestination = new JPanel();
	
	// Auto checkbox states
	private static boolean autoTrackCheckBoxSelected = false;
	private static boolean autoDestinationTrackCheckBoxSelected = false;
	private static boolean autoFinalDestTrackCheckBoxSelected = false;
	private static boolean autoReturnWhenEmptyTrackCheckBoxSelected = false;
		
	public RollingStockSetFrame() {
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
		JPanel pRs = new JPanel();
		pRs.setLayout(new GridBagLayout());
		pRs.setBorder(BorderFactory.createTitledBorder(getRb().getString("RollingStockType")));
		addItem(pRs, textRoad, 1, 0);
		pRow1.add(pRs);
		
		// row 1b
		JPanel pType = new JPanel();
		pType.setLayout(new GridBagLayout());
		pType.setBorder(BorderFactory.createTitledBorder(rb.getString("Type")));
		addItem(pType, textType, 1, 0);
		pRow1.add(pType);
		
		// row 1c
		JPanel pStatus = new JPanel();
		pStatus.setLayout(new GridBagLayout());
		pStatus.setBorder(BorderFactory.createTitledBorder(rb.getString("Status")));
		addItem(pStatus, locationUnknownCheckBox, 1, 0);
		addItem(pStatus, outOfServiceCheckBox, 1, 1);
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
		
		// optional panel return when empty
		pOptionalrwe.setLayout(new BoxLayout(pOptionalrwe,BoxLayout.Y_AXIS));
		pOptionalrwe.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptional")));
		
		// row 5
		JPanel pReturnWhenEmpty = new JPanel();
		pReturnWhenEmpty.setLayout(new GridBagLayout());
		pReturnWhenEmpty.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutReturnWhenEmpty")));
		addItem(pReturnWhenEmpty, textName4, 1, 0);
		addItem(pReturnWhenEmpty, textTrack4, 2, 0);
		addItem(pReturnWhenEmpty, destReturnWhenEmptyBox, 1, 1);
		addItem(pReturnWhenEmpty, trackReturnWhenEmptyBox, 2, 1);
		addItem(pReturnWhenEmpty, autoReturnWhenEmptyTrackCheckBox, 3, 1);
		pOptionalrwe.add(pReturnWhenEmpty);
		
		// optional panel 2
		JPanel pOptional2 = new JPanel();
		pOptional2.setLayout(new BoxLayout(pOptional2,BoxLayout.Y_AXIS));
		pOptional2.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptionalProgram")));

		// row 6
		JPanel pDestination = new JPanel();
		pDestination.setLayout(new GridBagLayout());
		pDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutDestination")));
		addItem(pDestination, textName2, 1, 0);
		addItem(pDestination, textTrack2, 2, 0);
		addItem(pDestination, destinationBox, 1, 1);
		addItem(pDestination, trackDestinationBox, 2, 1);
		addItem(pDestination, autoDestinationTrackCheckBox, 3, 1);
		pOptional2.add(pDestination);
		
		// row 7
		pFinalDestination.setLayout(new GridBagLayout());
		pFinalDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutFinalDestination")));
		addItem(pFinalDestination, textName3, 1, 0);
		addItem(pFinalDestination, textTrack3, 2, 0);
		addItem(pFinalDestination, finalDestinationBox, 1, 1);
		addItem(pFinalDestination, finalDestTrackBox, 2, 1);
		addItem(pFinalDestination, autoFinalDestTrackCheckBox, 3, 1);
		pOptional2.add(pFinalDestination);
		
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
		getContentPane().add(pOptionalrwe);
		getContentPane().add(pOptional2);
		getContentPane().add(pButtons);
				
		// setup buttons
		addButtonAction(saveButton);
		
		// setup combobox
		addComboBoxAction(locationBox);
		addComboBoxAction(destinationBox);
		addComboBoxAction(finalDestinationBox);
		addComboBoxAction(destReturnWhenEmptyBox);
		
		// setup checkbox
		addCheckBoxAction(locationUnknownCheckBox);
		addCheckBoxAction(outOfServiceCheckBox);
		addCheckBoxAction(autoTrackCheckBox);
		addCheckBoxAction(autoDestinationTrackCheckBox);
		addCheckBoxAction(autoFinalDestTrackCheckBox);
		addCheckBoxAction(autoReturnWhenEmptyTrackCheckBox);
		
		// set auto check box selected
		autoTrackCheckBox.setSelected(autoTrackCheckBoxSelected);
		autoDestinationTrackCheckBox.setSelected(autoDestinationTrackCheckBoxSelected);
		autoFinalDestTrackCheckBox.setSelected(autoFinalDestTrackCheckBoxSelected);
		autoReturnWhenEmptyTrackCheckBox.setSelected(autoReturnWhenEmptyTrackCheckBoxSelected);
		
		// add tool tips
		autoTrackCheckBox.setToolTipText(getRb().getString("TipAutoTrack"));
		autoDestinationTrackCheckBox.setToolTipText(getRb().getString("TipAutoTrack"));
		autoFinalDestTrackCheckBox.setToolTipText(rb.getString("TipAutoTrack"));
		autoReturnWhenEmptyTrackCheckBox.setToolTipText(rb.getString("TipAutoTrack"));

		//	 get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);
		// get notified if train combo box gets modified
		trainManager.addPropertyChangeListener(this);
				
		// set frame size and location for display
		setLocation(Control.panelX, Control.panelY);
		setMinimumSize(new Dimension(500, getHeight()));
	}
	
	public void load(RollingStock rs){
		_rs = rs;
		_rs.addPropertyChangeListener(this);
		textRoad.setText(_rs.getRoad()+" "+_rs.getNumber());
		textType.setText(_rs.getType());
		locationUnknownCheckBox.setSelected(_rs.isLocationUnknown());
		outOfServiceCheckBox.setSelected(_rs.isOutOfService());
		updateComboBoxes();
		enableComponents(!locationUnknownCheckBox.isSelected());
		if (_rs.getRouteLocation() != null)
			log.debug("rs has a pickup location "+_rs.getRouteLocation().getName());
		if (_rs.getRouteDestination() != null)
			log.debug("rs has a destination "+_rs.getRouteDestination().getName());
		// has the program generated a pickup and drop for this rolling stock?
		if (_rs.getRouteLocation() != null || _rs.getRouteDestination() != null){
			JOptionPane.showMessageDialog(this,
					getRb().getString("pressSaveWill"),	getRb().getString("rsInRoute"),
					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	// Save button
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton) {
			_disableComboBoxUpdate = true;	// need to stop property changes while we update
			save();
			_disableComboBoxUpdate = false;
		}
	}
	
	protected ResourceBundle getRb(){
		return rb;
	}
	
	protected boolean save(){
		log.debug("Save button action");
		// save the auto buttons
		autoTrackCheckBoxSelected = autoTrackCheckBox.isSelected();
		autoDestinationTrackCheckBoxSelected = autoDestinationTrackCheckBox.isSelected();
		autoFinalDestTrackCheckBoxSelected = autoFinalDestTrackCheckBox.isSelected();
		autoReturnWhenEmptyTrackCheckBoxSelected = autoReturnWhenEmptyTrackCheckBox.isSelected();
		
		// save the statuses
		_rs.setLocationUnknown(locationUnknownCheckBox.isSelected());
		_rs.setOutOfService(outOfServiceCheckBox.isSelected());
		
		if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
			_rs.setLocation(null, null);
		} else {
			if (trackLocationBox.getSelectedItem() == null
					|| trackLocationBox.getSelectedItem().equals("")) {
				JOptionPane.showMessageDialog(this,
						getRb().getString("rsFullySelect"),	getRb().getString("rsCanNotLoc"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			String status = _rs.setLocation((Location) locationBox.getSelectedItem(),
					(Track)trackLocationBox.getSelectedItem());
			if (!status.equals(RollingStock.OKAY)){
				log.debug ("Can't set rs's location because of "+ status);
				JOptionPane.showMessageDialog(this,
						getRb().getString("rsCanNotLocMsg")+ status,
						getRb().getString("rsCanNotLoc"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (destinationBox.getSelectedItem() == null || destinationBox.getSelectedItem().equals("")) {
			_rs.setDestination(null, null);
		} else {
			Track destTrack = null;
			if (trackDestinationBox.getSelectedItem() != null 
					&& !trackDestinationBox.getSelectedItem().equals("")){
				destTrack = (Track)trackDestinationBox.getSelectedItem();
			}
			Location destination = (Location) destinationBox.getSelectedItem();
			String status = _rs.setDestination(destination, destTrack);
			if (!status.equals(RollingStock.OKAY)){
				log.debug ("Can't set rs's destination because of "+ status);
				JOptionPane.showMessageDialog(this,
						getRb().getString("rsCanNotDestMsg")+ status,
						getRb().getString("rsCanNotDest"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals(""))
			_rs.setTrain(null);
		else {
			_rs.setTrain((Train)trainBox.getSelectedItem());
			Train train = _rs.getTrain();
			// determine if train services this rs's type
			if (train != null && !train.acceptsTypeName(_rs.getType())){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(getRb().getString("rsTrainNotServType"), new Object[]{_rs.getType(), train.getName()}),
						getRb().getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			// determine if train services this rs's road
			if (train != null && !train.acceptsRoadName(_rs.getRoad())){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(getRb().getString("rsTrainNotServRoad"), new Object[]{_rs.getRoad(), train.getName()}),
						getRb().getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			// determine if train services this rs's built date
			if (train != null && !train.acceptsBuiltDate(_rs.getBuilt())){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(getRb().getString("rsTrainNotServBuilt"), new Object[]{_rs.getBuilt(), train.getName()}),
						getRb().getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			// determine if train services this rs's built date
			if (train != null && !train.acceptsOwnerName(_rs.getOwner())){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(getRb().getString("rsTrainNotServOwner"), new Object[]{_rs.getOwner(), train.getName()}),
						getRb().getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			// determine if train services the location and destination selected by user
			RouteLocation rl = null;
			RouteLocation rd = null;
			Route route = null;
			if (train != null){
				route = train.getRoute();
				if (route != null){
					rl = route.getLastLocationByName(_rs.getLocationName());
					rd = route.getLastLocationByName(_rs.getDestinationName());
				}
			} else {
				log.error("Expected a train from combobox");
				return false;
			}
			if (rl == null){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(getRb().getString("rsLocNotServ"), new Object[]{_rs.getLocationName(), train.getName()}),
						getRb().getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (rd == null && !_rs.getDestinationName().equals("")){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(getRb().getString("rsDestNotServ"), new Object[]{_rs.getDestinationName(), train.getName()}),
						getRb().getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			} 
			if (rd != null && route != null){
				// now determine if destination is after location
				List<String> routeSequence = route.getLocationsBySequenceList();
				boolean foundLoc = false;	// when true, found the rs's location in the route
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
					JOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString("rsLocOrder"),
							new Object[] {_rs.getDestinationName(),	_rs.getLocationName(),
						train.getName() }), getRb().getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		// prevent rs from being picked up and delivered
		_rs.setRouteLocation(null);
		_rs.setRouteDestination(null);
		return true;
	}
	
	protected void updateComboBoxes(){
		if (_disableComboBoxUpdate)
			return;
		log.debug("update combo boxes");
		locationManager.updateComboBox(locationBox);
		locationBox.setSelectedItem(_rs.getLocation());	
		locationManager.updateComboBox(destinationBox);
		destinationBox.setSelectedItem(_rs.getDestination());
		locationManager.updateComboBox(finalDestinationBox);
		locationManager.updateComboBox(destReturnWhenEmptyBox);
		trainManager.updateComboBox(trainBox);
		trainBox.setSelectedItem(_rs.getTrain());
		
		// update track combo boxes
		updateLocation();
		updateDestination();
	}
	
	protected boolean updateGroup(List<RollingStock> list){
		for(int i=0; i<list.size(); i++){
			RollingStock rs = list.get(i);
			if (rs == _rs)
				continue;
			if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
				rs.setLocation(null, null);
			} else {
				String status = rs.setLocation((Location) locationBox.getSelectedItem(),
						(Track)trackLocationBox.getSelectedItem());
				if (!status.equals(RollingStock.OKAY)){
					log.debug ("Can't set the location for all of the rolling stock in the group because of "+ status);
					JOptionPane.showMessageDialog(this,
							getRb().getString("rsCanNotLocMsg")+ status,
							getRb().getString("rsCanNotLoc"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			if (destinationBox.getSelectedItem() == null || destinationBox.getSelectedItem().equals("")) {
				rs.setDestination(null, null);
			} else {
				Track track = null;
				if (trackDestinationBox.getSelectedItem() != null && !trackDestinationBox.getSelectedItem().equals(""))
					track = (Track)trackDestinationBox.getSelectedItem();
				String status = rs.setDestination((Location)destinationBox.getSelectedItem(), track);
				if (!status.equals(RollingStock.OKAY)){
					log.debug ("Can't set the destination for all of the rolling stock in the group because of "+ status);
					JOptionPane.showMessageDialog(this,
							getRb().getString("rsCanNotDestMsg")+ status,
							getRb().getString("rsCanNotDest"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
			if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals("")){
				rs.setTrain(null);
			} else {
				rs.setTrain((Train)trainBox.getSelectedItem());
			}
			// Location status and out of service
			rs.setLocationUnknown(locationUnknownCheckBox.isSelected());
			rs.setOutOfService(outOfServiceCheckBox.isSelected());
			// remove rolling stock from being picked up and delivered
			rs.setRouteLocation(null);
			rs.setRouteDestination(null);
		}
		return true;
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource()== locationBox){
			updateLocation();
		}
		if (ae.getSource()== destinationBox){
			updateDestination();
		}
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("checkbox action ");
		if (ae.getSource() == locationUnknownCheckBox){
			outOfServiceCheckBox.setSelected(locationUnknownCheckBox.isSelected());
			enableComponents(!locationUnknownCheckBox.isSelected());
		}
		if (ae.getSource() == autoTrackCheckBox) 
			updateLocation();
		if (ae.getSource() == autoDestinationTrackCheckBox) 
			updateDestination();
	}
	
	protected void enableComponents(boolean enabled){
		// combo boxes
		locationBox.setEnabled(enabled);
		trackLocationBox.setEnabled(enabled); 
		destinationBox.setEnabled(enabled);
		trackDestinationBox.setEnabled(enabled); 
		destReturnWhenEmptyBox.setEnabled(enabled);
		trackReturnWhenEmptyBox.setEnabled(enabled); 
		finalDestinationBox.setEnabled(enabled);
		finalDestTrackBox.setEnabled(enabled);
		trainBox.setEnabled(enabled);
		// checkboxes
		autoTrackCheckBox.setEnabled(enabled);
		autoDestinationTrackCheckBox.setEnabled(enabled);
		autoFinalDestTrackCheckBox.setEnabled(enabled);
		autoReturnWhenEmptyTrackCheckBox.setEnabled(enabled);
		outOfServiceCheckBox.setEnabled(enabled);
	}
	
	protected void updateLocation(){
		if (locationBox.getSelectedItem() != null){
			if (locationBox.getSelectedItem().equals("")){
				trackLocationBox.removeAllItems();
			}else{
				log.debug("CarSetFrame sees location: "+ locationBox.getSelectedItem());
				Location l = (Location)locationBox.getSelectedItem();
				l.updateComboBox(trackLocationBox, _rs, autoTrackCheckBox.isSelected(), false);
				if (_rs.getLocation() != null && _rs.getLocation().equals(l) && _rs.getTrack() != null)
					trackLocationBox.setSelectedItem(_rs.getTrack());
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
				l.updateComboBox(trackDestinationBox, _rs, autoDestinationTrackCheckBox.isSelected(), true);
				if (_rs.getDestination() != null && _rs.getDestination().equals(l) && _rs.getDestinationTrack() != null)
					trackDestinationBox.setSelectedItem(_rs.getDestinationTrack());
				packFrame();
			}
		}
	}
	
	
	protected void packFrame(){
		pack();
		/*
		if ((getWidth()<450)) 
			setSize(500, getHeight()+50);
		else
			setSize (getWidth()+50, getHeight()+50);
		*/		
		setVisible(true);
	}

	public void dispose(){
		if (_rs != null)
			_rs.removePropertyChangeListener(this);
		LocationManager.instance().removePropertyChangeListener(this);
		trainManager.removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("PropertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY) ||
				e.getSource().getClass().equals(Car.class) ||
				e.getSource().getClass().equals(Engine.class)){
			updateComboBoxes();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(RollingStockSetFrame.class.getName());
}
