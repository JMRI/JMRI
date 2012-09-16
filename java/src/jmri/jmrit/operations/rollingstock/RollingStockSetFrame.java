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
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;


/**
 * Frame for user to place RollingStock on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2010, 2011, 2012
 * @version $Revision$
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
	protected JCheckBox autoTrainCheckBox = new JCheckBox(rb.getString("Auto"));
	
	protected JCheckBox locationUnknownCheckBox = new JCheckBox(rb.getString("LocationUnknown"));
	protected JCheckBox outOfServiceCheckBox = new JCheckBox(rb.getString("OutOfService"));
	
	protected JCheckBox ignoreStatusCheckBox = new JCheckBox(rb.getString("Ignore"));
	protected JCheckBox ignoreLocationCheckBox = new JCheckBox(rb.getString("Ignore"));
	protected JCheckBox ignoreRWECheckBox = new JCheckBox(rb.getString("Ignore"));	
	protected JCheckBox ignoreDestinationCheckBox = new JCheckBox(rb.getString("Ignore"));
	protected JCheckBox ignoreFinalDestinationCheckBox = new JCheckBox(rb.getString("Ignore"));
	protected JCheckBox ignoreTrainCheckBox = new JCheckBox(rb.getString("Ignore"));
	
	// optional panels
	protected JPanel pOptional = new JPanel();
	protected JPanel pFinalDestination = new JPanel();
	
	// Auto checkbox states
	private static boolean autoTrackCheckBoxSelected = false;
	private static boolean autoDestinationTrackCheckBoxSelected = false;
	private static boolean autoFinalDestTrackCheckBoxSelected = false;
	private static boolean autoReturnWhenEmptyTrackCheckBoxSelected = false;
	private static boolean autoTrainCheckBoxSelected = false;
		
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
		addItem(pStatus, ignoreStatusCheckBox, 1, 0);
		addItem(pStatus, locationUnknownCheckBox, 1, 1);
		addItem(pStatus, outOfServiceCheckBox, 1, 2);
		pRow1.add(pStatus);
		
		pPanel.add(pRow1);
	
		// row 2
		JPanel pLocation = new JPanel();
		pLocation.setLayout(new GridBagLayout());
		pLocation.setBorder(BorderFactory.createTitledBorder(rb.getString("Location")));
		addItem(pLocation, textName, 1, 0);
		addItem(pLocation, textTrack, 2, 0);
		addItem(pLocation, ignoreLocationCheckBox, 0, 1);
		addItem(pLocation, locationBox, 1, 1);
		addItem(pLocation, trackLocationBox, 2, 1);
		addItem(pLocation, autoTrackCheckBox, 3, 1);
		pPanel.add(pLocation);
		
		// optional panel return when empty
		pOptional.setLayout(new BoxLayout(pOptional,BoxLayout.Y_AXIS));
		pOptional.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutOptional")));
		
		// row 5
		JPanel pReturnWhenEmpty = new JPanel();
		pReturnWhenEmpty.setLayout(new GridBagLayout());
		pReturnWhenEmpty.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutReturnWhenEmpty")));
		addItem(pReturnWhenEmpty, textName4, 1, 0);
		addItem(pReturnWhenEmpty, textTrack4, 2, 0);
		addItem(pReturnWhenEmpty, ignoreRWECheckBox, 0, 1);
		addItem(pReturnWhenEmpty, destReturnWhenEmptyBox, 1, 1);
		addItem(pReturnWhenEmpty, trackReturnWhenEmptyBox, 2, 1);
		addItem(pReturnWhenEmpty, autoReturnWhenEmptyTrackCheckBox, 3, 1);
		pOptional.add(pReturnWhenEmpty);
		
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
		addItem(pDestination, ignoreDestinationCheckBox, 0, 1);
		addItem(pDestination, destinationBox, 1, 1);
		addItem(pDestination, trackDestinationBox, 2, 1);
		addItem(pDestination, autoDestinationTrackCheckBox, 3, 1);
		pOptional2.add(pDestination);
		
		// row 7
		pFinalDestination.setLayout(new GridBagLayout());
		pFinalDestination.setBorder(BorderFactory.createTitledBorder(rb.getString("BorderLayoutFinalDestination")));
		addItem(pFinalDestination, textName3, 1, 0);
		addItem(pFinalDestination, textTrack3, 2, 0);
		addItem(pFinalDestination, ignoreFinalDestinationCheckBox, 0, 1);
		addItem(pFinalDestination, finalDestinationBox, 1, 1);
		addItem(pFinalDestination, finalDestTrackBox, 2, 1);
		addItem(pFinalDestination, autoFinalDestTrackCheckBox, 3, 1);
		pOptional2.add(pFinalDestination);
		
		// row 8
		JPanel pTrain = new JPanel();
		pTrain.setLayout(new GridBagLayout());
		pTrain.setBorder(BorderFactory.createTitledBorder(rb.getString("Train")));
		addItem(pTrain, ignoreTrainCheckBox, 0, 0);
		addItem(pTrain, trainBox, 1, 0);
		addItem(pTrain, autoTrainCheckBox, 2, 0);
		pOptional2.add(pTrain);

		// button panel
		JPanel pButtons = new JPanel();
		pButtons.setLayout(new GridBagLayout());
		addItem(pButtons, saveButton, 2, 10);
		
		// add panels
		getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
		getContentPane().add(pPanel);
		getContentPane().add(pOptional);
		getContentPane().add(pOptional2);
		getContentPane().add(pButtons);
		
		// Don't show ignore buttons
		ignoreStatusCheckBox.setVisible(false);
		ignoreLocationCheckBox.setVisible(false);
		ignoreRWECheckBox.setVisible(false);
		ignoreDestinationCheckBox.setVisible(false);
		ignoreFinalDestinationCheckBox.setVisible(false);
		ignoreTrainCheckBox.setVisible(false);
				
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
		addCheckBoxAction(autoTrainCheckBox);
		
		addCheckBoxAction(ignoreStatusCheckBox);
		addCheckBoxAction(ignoreLocationCheckBox);
		addCheckBoxAction(ignoreRWECheckBox);
		addCheckBoxAction(ignoreDestinationCheckBox);
		addCheckBoxAction(ignoreFinalDestinationCheckBox);
		addCheckBoxAction(ignoreTrainCheckBox);
		
		// set auto check box selected
		autoTrackCheckBox.setSelected(autoTrackCheckBoxSelected);
		autoDestinationTrackCheckBox.setSelected(autoDestinationTrackCheckBoxSelected);
		autoFinalDestTrackCheckBox.setSelected(autoFinalDestTrackCheckBoxSelected);
		autoReturnWhenEmptyTrackCheckBox.setSelected(autoReturnWhenEmptyTrackCheckBoxSelected);
		autoTrainCheckBox.setSelected(autoTrainCheckBoxSelected);
		
		// add tool tips
		autoTrackCheckBox.setToolTipText(getRb().getString("TipAutoTrack"));
		autoDestinationTrackCheckBox.setToolTipText(getRb().getString("TipAutoTrack"));
		autoFinalDestTrackCheckBox.setToolTipText(rb.getString("TipAutoTrack"));
		autoReturnWhenEmptyTrackCheckBox.setToolTipText(rb.getString("TipAutoTrack"));
		autoTrainCheckBox.setToolTipText(rb.getString("TipAutoTrain"));
		
		ignoreStatusCheckBox.setToolTipText(getRb().getString("TipIgnore"));
		ignoreLocationCheckBox.setToolTipText(getRb().getString("TipIgnore"));
		ignoreRWECheckBox.setToolTipText(getRb().getString("TipIgnore"));
		ignoreDestinationCheckBox.setToolTipText(getRb().getString("TipIgnore"));
		ignoreFinalDestinationCheckBox.setToolTipText(getRb().getString("TipIgnore"));
		ignoreTrainCheckBox.setToolTipText(getRb().getString("TipIgnore"));

		//	 get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);
		// get notified if train combo box gets modified
		trainManager.addPropertyChangeListener(this);
				
		setMinimumSize(new Dimension(500, Control.panelHeight));
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
		// has the program generated a pick up and set out for this rolling stock?
		if (_rs.getRouteLocation() != null || _rs.getRouteDestination() != null){
			if (_rs.getRouteLocation() != null)
				log.debug("rs has a pick up location "+_rs.getRouteLocation().getName());
			if (_rs.getRouteDestination() != null)
				log.debug("rs has a destination "+_rs.getRouteDestination().getName());
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
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}
	
	protected ResourceBundle getRb(){
		return rb;
	}
	
	protected boolean save(){
		return change(_rs);
	}
	
	// change(RollingStock rs) will load the route location and the route destination if possible
	RouteLocation rl;
	RouteLocation rd;
	protected boolean change(RollingStock rs){
		log.debug("Change button action");
		// save the auto buttons
		autoTrackCheckBoxSelected = autoTrackCheckBox.isSelected();
		autoDestinationTrackCheckBoxSelected = autoDestinationTrackCheckBox.isSelected();
		autoFinalDestTrackCheckBoxSelected = autoFinalDestTrackCheckBox.isSelected();
		autoReturnWhenEmptyTrackCheckBoxSelected = autoReturnWhenEmptyTrackCheckBox.isSelected();
		autoTrainCheckBoxSelected = autoTrainCheckBox.isSelected();
		
		// save the statuses
		if (!ignoreStatusCheckBox.isSelected()){
			rs.setLocationUnknown(locationUnknownCheckBox.isSelected());
			rs.setOutOfService(outOfServiceCheckBox.isSelected());
		}		
		// update location and destination
		if (!changeLocation(rs))
			return false;
		// check to see if rolling stock is in staging and out of service (also location unknown)
		if (outOfServiceCheckBox.isSelected() && rs.getTrack()!=null && rs.getTrack().getLocType().equals(Track.STAGING)){
			JOptionPane.showMessageDialog(this,
					getRb().getString("rsNeedToRemoveStaging"),
					getRb().getString("rsInStaging"),
					JOptionPane.WARNING_MESSAGE);
			// clear the rolling stock's location
			locationBox.setSelectedItem("");
			rs.setLocation(null, null);
		}
		
		loadTrain(rs);
		
		if (!changeDestination(rs))
			return false;

		if (!ignoreTrainCheckBox.isSelected()){	
			Train train = rs.getTrain();
			if (train != null){
				// determine if train services this rs's type
				if (!train.acceptsTypeName(rs.getType())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(getRb().getString("rsTrainNotServType"), new Object[]{rs.getType(), train.getName()}),
							getRb().getString("rsNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				// determine if train services this rs's road
				if (!train.acceptsRoadName(rs.getRoad())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(getRb().getString("rsTrainNotServRoad"), new Object[]{rs.getRoad(), train.getName()}),
							getRb().getString("rsNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				// determine if train services this rs's built date
				if (!train.acceptsBuiltDate(rs.getBuilt())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(getRb().getString("rsTrainNotServBuilt"), new Object[]{rs.getBuilt(), train.getName()}),
							getRb().getString("rsNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				// determine if train services this rs's built date
				if (!train.acceptsOwnerName(rs.getOwner())){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(getRb().getString("rsTrainNotServOwner"), new Object[]{rs.getOwner(), train.getName()}),
							getRb().getString("rsNotMove"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				// determine if train services the location and destination selected by user
				rl = null;
				rd = null;
				if (rs.getLocation() != null){
					Route route = train.getRoute();
					if (route != null){
						rl = route.getLastLocationByName(rs.getLocationName());
						rd = route.getLastLocationByName(rs.getDestinationName());
					}
					if (rl == null){
						JOptionPane.showMessageDialog(this,
								MessageFormat.format(getRb().getString("rsLocNotServ"), new Object[]{rs.getLocationName(), train.getName()}),
								getRb().getString("rsNotMove"),
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					if (rd == null && !rs.getDestinationName().equals("")){
						JOptionPane.showMessageDialog(this,
								MessageFormat.format(getRb().getString("rsDestNotServ"), new Object[]{rs.getDestinationName(), train.getName()}),
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
							RouteLocation rlocation = route.getLocationById(routeSequence.get(i));
							if (rs.getLocationName().equals(rlocation.getName())){
								rl = rlocation;
								foundLoc = true;
							}
							if (rs.getDestinationName().equals(rlocation.getName()) && foundLoc){
								foundDes = true;
								break;
							}

						}
						if (!foundDes){
							JOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString("rsLocOrder"),
									new Object[] {rs.getDestinationName(),	rs.getLocationName(),
								train.getName() }), getRb().getString("rsNotMove"),
								JOptionPane.ERROR_MESSAGE);
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private boolean changeLocation(RollingStock rs){
		if (!ignoreLocationCheckBox.isSelected()){
			if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
				rs.setLocation(null, null);
			} else {
				if (trackLocationBox.getSelectedItem() == null
						|| trackLocationBox.getSelectedItem().equals("")) {
					JOptionPane.showMessageDialog(this,
							getRb().getString("rsFullySelect"),	getRb().getString("rsCanNotLoc"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
				// update location only if it has changed
				if (rs.getLocation() == null || !rs.getLocation().equals(locationBox.getSelectedItem()) 
						|| rs.getTrack() == null || !rs.getTrack().equals(trackLocationBox.getSelectedItem())){
					String status = rs.setLocation((Location) locationBox.getSelectedItem(),
							(Track)trackLocationBox.getSelectedItem());
					if (!status.equals(Track.OKAY)){
						log.debug ("Can't set rs's location because of "+ status);
						JOptionPane.showMessageDialog(this,
								MessageFormat.format(getRb().getString("rsCanNotLocMsg"), new Object[]{rs.toString(), status}),
								getRb().getString("rsCanNotLoc"),
								JOptionPane.ERROR_MESSAGE);
						// does the user want to force the rolling stock to this track?
						int results = JOptionPane.showOptionDialog(this, 
								MessageFormat.format(getRb().getString("rsForce"), new Object[]{rs.toString(), (Track)trackLocationBox.getSelectedItem()}),
								MessageFormat.format(getRb().getString("rsOverride"), new Object[]{status}),
								JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
						if (results == JOptionPane.YES_OPTION) {
							log.debug("Force rolling stock to track");
							rs.setLocation((Location) locationBox.getSelectedItem(),
									(Track)trackLocationBox.getSelectedItem(), true);
						} else {
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private void loadTrain(RollingStock rs){
		if (!ignoreTrainCheckBox.isSelected()){
			if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals("")){
				if (rs.getTrain() != null){
					// prevent rs from being picked up and delivered
					setRouteLocationAndDestination(rs, rs.getTrain(), null, null);
				}
				rs.setTrain(null);
			} else {
				Train train = (Train)trainBox.getSelectedItem();
				if (rs.getTrain() != null && !rs.getTrain().equals(train))
					// prevent rs from being picked up and delivered
					setRouteLocationAndDestination(rs, rs.getTrain(), null, null);
				rs.setTrain(train);
			}
		}
	}
	
	private boolean changeDestination(RollingStock rs){
		if (!ignoreDestinationCheckBox.isSelected()){
			if (destinationBox.getSelectedItem() == null || destinationBox.getSelectedItem().equals("")) {
				rs.setDestination(null, null);
			} else {
				Track destTrack = null;
				if (trackDestinationBox.getSelectedItem() != null 
						&& !trackDestinationBox.getSelectedItem().equals("")){
					destTrack = (Track)trackDestinationBox.getSelectedItem();
				}
				if (destTrack != null && rs.getDestinationTrack() != destTrack && destTrack.getLocType().equals(Track.STAGING)
						&& (rs.getTrain() == null || !rs.getTrain().isBuilt())){
					log.debug ("Destination track ("+destTrack.getName()+") is staging");
					JOptionPane.showMessageDialog(this,
							getRb().getString("rsDoNotSelectStaging"),
							getRb().getString("rsCanNotDest"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}	
				String status = rs.setDestination((Location) destinationBox.getSelectedItem(), destTrack);
				if (!status.equals(Track.OKAY)){
					log.debug ("Can't set rs's destination because of "+ status);
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(getRb().getString("rsCanNotDestMsg"), new Object[]{rs.toString(), status}),
							getRb().getString("rsCanNotDest"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}	
			}
		}
		return true;
	}
	
	protected void checkTrain(RollingStock rs){
		// determine if train is built and car is part of train or wants to be part of the train
		Train train = rs.getTrain(); 
		if (train != null && train.isBuilt()){
			if (rs.getRouteLocation() != null && rs.getRouteDestination() != null 
					&& rl != null && rd != null
					&& (!rs.getRouteLocation().getName().equals(rl.getName()) 
							|| !rs.getRouteDestination().getName().equals(rd.getName())
							|| rs.getDestinationTrack() == null)){
				// user changed rolling stock location or destination or no destination track
				setRouteLocationAndDestination(rs, train, null, null);
			}
			if (rs.getRouteLocation() != null || rs.getRouteDestination() != null){
				if (JOptionPane.showConfirmDialog(this, 
						MessageFormat.format(getRb().getString("rsRemoveRsFromTrain"), new Object[]{rs.toString(), train.getName()}),
						getRb().getString("rsInRoute"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					// prevent rs from being picked up and delivered
					setRouteLocationAndDestination(rs, train, null, null);
				}
			} else if (rl != null && rd != null && rs.getDestinationTrack() != null  && !train.isTrainInRoute()){
				if (JOptionPane.showConfirmDialog(this, 
						MessageFormat.format(getRb().getString("rsAddRsToTrain"), new Object[]{rs.toString(), train.getName()}),
						getRb().getString("rsAddManuallyToTrain"),						
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					// set new pick up and set out locations
					setRouteLocationAndDestination(rs, train, rl, rd);
					log.debug("Add rolling stock ("+rs.toString()+") to train "+train.getName()+" route pick up "+rl.getId()+" drop "+rd.getId());
				}
			}		
		}
	}
	
	protected void setRouteLocationAndDestination(RollingStock rs, Train train, RouteLocation rl, RouteLocation rd){
		if (rs.getRouteLocation() != null || rl != null)
			train.setModified(true);
		rs.setRouteLocation(rl);
		rs.setRouteDestination(rd);
	}
	
	protected void updateComboBoxes(){
		log.debug("update combo boxes");
		locationManager.updateComboBox(locationBox);
		locationManager.updateComboBox(destinationBox);
		locationManager.updateComboBox(finalDestinationBox);
		locationManager.updateComboBox(destReturnWhenEmptyBox);
		trainManager.updateComboBox(trainBox);
		if (_rs != null){
			locationBox.setSelectedItem(_rs.getLocation());
			destinationBox.setSelectedItem(_rs.getDestination());
			trainBox.setSelectedItem(_rs.getTrain());
		}
		
		// update track combo boxes
		updateLocation();
		updateDestination();
	}
	
	protected boolean updateGroup(List<RollingStock> list){
		for(int i=0; i<list.size(); i++){
			RollingStock rs = list.get(i);
			if (rs == _rs)
				continue;
			// Location status and out of service
			if (!ignoreStatusCheckBox.isSelected()){
				rs.setLocationUnknown(locationUnknownCheckBox.isSelected());
				rs.setOutOfService(outOfServiceCheckBox.isSelected());
			}
			// update location and destination
			if (!changeLocation(rs))
				return false;
			if (!changeDestination(rs))
				return false;

			if (!ignoreTrainCheckBox.isSelected()){
				if (trainBox.getSelectedItem() == null || trainBox.getSelectedItem().equals("")){
					rs.setTrain(null);
				} else {
					rs.setTrain((Train)trainBox.getSelectedItem());
				}
			}
			// set the route location and destination to match
			rs.setRouteLocation(_rs.getRouteLocation());
			rs.setRouteDestination(_rs.getRouteDestination());
		}
		return true;
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("Combobox action");
		if (_disableComboBoxUpdate)
			return;
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
		if (ae.getSource() == ignoreStatusCheckBox){
			locationUnknownCheckBox.setEnabled(!ignoreStatusCheckBox.isSelected());
			outOfServiceCheckBox.setEnabled(!ignoreStatusCheckBox.isSelected());
		}
		if (ae.getSource() == ignoreLocationCheckBox){
			locationBox.setEnabled(!ignoreLocationCheckBox.isSelected());
			trackLocationBox.setEnabled(!ignoreLocationCheckBox.isSelected());
			autoTrackCheckBox.setEnabled(!ignoreLocationCheckBox.isSelected());
		}
		if (ae.getSource() == ignoreRWECheckBox){
			destReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected());
			trackReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected());
			autoReturnWhenEmptyTrackCheckBox.setEnabled(!ignoreRWECheckBox.isSelected());
		}
		if (ae.getSource() == ignoreDestinationCheckBox){
			destinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected());
			trackDestinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected());
			autoDestinationTrackCheckBox.setEnabled(!ignoreDestinationCheckBox.isSelected());
		}
		if (ae.getSource() == ignoreFinalDestinationCheckBox){
			finalDestinationBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected());
			finalDestTrackBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected());
			autoFinalDestTrackCheckBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected());
		}
		if (ae.getSource() == ignoreTrainCheckBox){
			trainBox.setEnabled(!ignoreTrainCheckBox.isSelected());
			autoTrainCheckBox.setEnabled(!ignoreTrainCheckBox.isSelected());
		}
	}
	
	protected void enableComponents(boolean enabled){
		// combo boxes
		locationBox.setEnabled(!ignoreLocationCheckBox.isSelected() & enabled);
		trackLocationBox.setEnabled(!ignoreLocationCheckBox.isSelected() & enabled); 
		destinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enabled);
		trackDestinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enabled); 
		destReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected() & enabled);
		trackReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected() & enabled); 
		finalDestinationBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected() & enabled);
		finalDestTrackBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected() & enabled);
		trainBox.setEnabled(!ignoreTrainCheckBox.isSelected() & enabled);
		// checkboxes
		autoTrackCheckBox.setEnabled(!ignoreLocationCheckBox.isSelected() & enabled);
		autoDestinationTrackCheckBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enabled);
		autoFinalDestTrackCheckBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected() & enabled);
		autoReturnWhenEmptyTrackCheckBox.setEnabled(!ignoreRWECheckBox.isSelected() & enabled);
		autoTrainCheckBox.setEnabled(!ignoreTrainCheckBox.isSelected() & enabled);
		locationUnknownCheckBox.setEnabled(!ignoreStatusCheckBox.isSelected());
		outOfServiceCheckBox.setEnabled(!ignoreStatusCheckBox.isSelected() & enabled);
		
		ignoreStatusCheckBox.setEnabled(enabled);
		ignoreLocationCheckBox.setEnabled(enabled);
		ignoreRWECheckBox.setEnabled(enabled);
		ignoreDestinationCheckBox.setEnabled(enabled);
		ignoreFinalDestinationCheckBox.setEnabled(enabled);
		ignoreTrainCheckBox.setEnabled(enabled);
	}
	
	protected void updateLocation(){
		if (locationBox.getSelectedItem() != null){
			if (locationBox.getSelectedItem().equals("")){
				trackLocationBox.removeAllItems();
			}else{
				log.debug("CarSetFrame sees location: "+ locationBox.getSelectedItem());
				Location l = (Location)locationBox.getSelectedItem();
				l.updateComboBox(trackLocationBox, _rs, autoTrackCheckBox.isSelected(), false);
				if (_rs != null && _rs.getLocation() != null && _rs.getLocation().equals(l) && _rs.getTrack() != null)
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
				if (_rs != null && _rs.getDestination() != null && _rs.getDestination().equals(l) && _rs.getDestinationTrack() != null)
					trackDestinationBox.setSelectedItem(_rs.getDestinationTrack());
				packFrame();
			}
		}
	}
	
	
	protected void packFrame(){
		pack();	
		if (getHeight()<400)
			setSize(getWidth(), 400);
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
		if (_disableComboBoxUpdate)
			return;
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
