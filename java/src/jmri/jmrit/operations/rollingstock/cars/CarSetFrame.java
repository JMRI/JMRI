// CarSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockSetFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;


/**
 * Frame for user to place car on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011
 * @version $Revision$
 */

public class CarSetFrame extends RollingStockSetFrame implements java.beans.PropertyChangeListener {

	protected static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	
	Car _car;
	
	// combo boxes
	JComboBox loadComboBox = CarLoads.instance().getComboBox(null);
	
	// buttons
	JButton editLoadButton = new JButton(Bundle.getMessage("Edit"));	
	
	// check boxes
	protected JCheckBox ignoreLoadCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));
	
	CarLoadEditFrame lef = null;
		
	public CarSetFrame() {
		super(Bundle.getMessage("TitleCarSet"));
	}

	public void initComponents() {
		super.initComponents();

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_CarsSet", true); // NOI18N
		
		// Only show nextDestination if routing enabled
		pOptional.setVisible(true);
		pFinalDestination.setVisible(Setup.isCarRoutingEnabled());
		
		// add load fields
		JPanel pLoad = new JPanel();
		pLoad.setLayout(new GridBagLayout());
		pLoad.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));
		addItem(pLoad, ignoreLoadCheckBox, 1, 0);
		addItem(pLoad, loadComboBox, 2, 0);
		addItem(pLoad, editLoadButton, 3, 0);
		pOptional.add(pLoad);
		
		// don't show ignore load checkbox
		ignoreLoadCheckBox.setVisible(false);
		
		// setup combobox
		addComboBoxAction(loadComboBox);
		
		// setup button
		addButtonAction(editLoadButton);
		
		// setup checkboxes
		addCheckBoxAction(ignoreLoadCheckBox);
		
		// tool tips
		ignoreLoadCheckBox.setToolTipText(getRb().getString("TipIgnore"));
		outOfServiceCheckBox.setToolTipText(getRb().getString("TipCarOutOfService"));
		
		// get notified if combo box gets modified
		CarLoads.instance().addPropertyChangeListener(this);
		
		packFrame();
	}
	
	public void loadCar(Car car){
		_car = car;
		load(car);
	}
	
	protected void updateComboBoxes(){
		super.updateComboBoxes();
		finalDestinationBox.setSelectedItem(_car.getNextDestination());
		destReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestination());
		
		updateFinalDestination();
		updateReturnWhenEmpty();
		updateLoadComboBox();
		updateTrainComboBox();
	}
	
	protected void enableComponents(boolean enabled){
		super.enableComponents(enabled);
		ignoreLoadCheckBox.setEnabled(enabled);
		loadComboBox.setEnabled(!ignoreLoadCheckBox.isSelected() & enabled);
		editLoadButton.setEnabled(!ignoreLoadCheckBox.isSelected() & enabled & _car != null);
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		_disableComboBoxUpdate = true;	// stop updates
		super.comboBoxActionPerformed(ae);
		if (ae.getSource()== finalDestinationBox){
			updateFinalDestination();
		}
		if (ae.getSource()== destReturnWhenEmptyBox){
			updateReturnWhenEmpty();
		}
		_disableComboBoxUpdate = false;
	}
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		super.buttonActionPerformed(ae);
		if (ae.getSource()== editLoadButton && _car != null){
			if (lef != null)
				lef.dispose();
			lef = new CarLoadEditFrame();
			lef.setLocationRelativeTo(this);
			lef.initComponents(_car.getType(), (String)loadComboBox.getSelectedItem());		
		}
	}
	
	protected boolean save(){
		if (change(_car)){
			OperationsXml.save();
			return true;
		}
		return false;
	}
	
	protected boolean change(Car car){
		// set final destination fields before destination in case there's a schedule at destination
		if (!ignoreFinalDestinationCheckBox.isSelected()){
			if (finalDestinationBox.getSelectedItem() == null || finalDestinationBox.getSelectedItem().equals("")) {
				car.setNextDestination(null);
				car.setNextDestTrack(null);
			} else {
				Track finalDestTrack = null;
				if (finalDestTrackBox.getSelectedItem() != null 
						&& !finalDestTrackBox.getSelectedItem().equals(""))
					finalDestTrack = (Track)finalDestTrackBox.getSelectedItem();
				if (finalDestTrack != null && car.getNextDestTrack() != finalDestTrack && finalDestTrack.getLocType().equals(Track.STAGING)){
					log.debug ("Destination track ("+finalDestTrack.getName()+") is staging");
					JOptionPane.showMessageDialog(this,
							getRb().getString("rsDoNotSelectStaging"),
							getRb().getString("rsCanNotFinal"),
							JOptionPane.ERROR_MESSAGE);
					return false;
				}	
				String status = car.testDestination((Location) finalDestinationBox.getSelectedItem(), finalDestTrack);
				if (!status.equals(Track.OKAY)){
					JOptionPane.showMessageDialog(this,
							MessageFormat.format(Bundle.getMessage("rsCanNotFinalMsg"), new Object[]{car.toString(), status}),
							Bundle.getMessage("rsCanNotFinal"),
							JOptionPane.WARNING_MESSAGE);
				}
				car.setNextDestination((Location) finalDestinationBox.getSelectedItem());
				car.setNextDestTrack(finalDestTrack);
			}
		}
		// car load
		if (!ignoreLoadCheckBox.isSelected() && loadComboBox.getSelectedItem() != null){
			String load = (String)loadComboBox.getSelectedItem();
			if (CarLoads.instance().containsName(car.getType(), load))
				car.setLoad(load);
		}
		// save car's track
		Track saveTrack = car.getTrack();
		if (!super.change(car))
			return false;
		// return when empty fields
		if (!ignoreRWECheckBox.isSelected()){
			if (destReturnWhenEmptyBox.getSelectedItem() == null || destReturnWhenEmptyBox.getSelectedItem().equals("")) {
				car.setReturnWhenEmptyDestination(null);
				car.setReturnWhenEmptyDestTrack(null);
			} else {
				if (trackReturnWhenEmptyBox.getSelectedItem() != null 
						&& !trackReturnWhenEmptyBox.getSelectedItem().equals("")){
					Track rwe = (Track)trackReturnWhenEmptyBox.getSelectedItem();
					if (rwe != null && rwe.getLocType().equals(Track.STAGING)){
						log.debug ("Return when empty track ("+rwe.getName()+") is staging");
						JOptionPane.showMessageDialog(this,
								getRb().getString("rsDoNotSelectStaging"),
								getRb().getString("rsCanNotRWE"),
								JOptionPane.ERROR_MESSAGE);
						return false;
					}
					String status = car.testDestination((Location) destReturnWhenEmptyBox.getSelectedItem(), (Track)trackReturnWhenEmptyBox.getSelectedItem());
					if (!status.equals(Track.OKAY)){
						JOptionPane.showMessageDialog(this,
								MessageFormat.format(Bundle.getMessage("rsCanNotRWEMsg"), new Object[]{car.toString(), status}),
								Bundle.getMessage("rsCanNotRWE"),
								JOptionPane.WARNING_MESSAGE);
					}
					car.setReturnWhenEmptyDestTrack((Track)trackReturnWhenEmptyBox.getSelectedItem());
				} else {
					car.setReturnWhenEmptyDestTrack(null);
				}
				car.setReturnWhenEmptyDestination((Location) destReturnWhenEmptyBox.getSelectedItem());
			}
		}
		// check to see if there's a schedule when placing the car at a spur
		if (!ignoreLocationCheckBox.isSelected() && trackLocationBox.getSelectedItem() != null && !trackLocationBox.getSelectedItem().equals("") 
				&& saveTrack != trackLocationBox.getSelectedItem()){
			Track track = (Track)trackLocationBox.getSelectedItem();
			if (track.getSchedule() != null){
				if (JOptionPane.showConfirmDialog(this,
						MessageFormat.format(getRb().getString("rsDoYouWantSchedule"),new Object[]{car.toString()}),
						MessageFormat.format(getRb().getString("rsSpurHasSchedule"),new Object[]{track.getName(), track.getScheduleName()}),					
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					String results = car.testSchedule(track);
					if (!results.equals(Track.OKAY)){
						JOptionPane.showMessageDialog(this,
								MessageFormat.format(getRb().getString("rsNotAbleToApplySchedule"),new Object[]{results}),
								getRb().getString("rsApplyingScheduleFailed"),
								JOptionPane.ERROR_MESSAGE);
						// restore previous location and track so we'll ask to test schedule again
						if (saveTrack != null)
							car.setLocation(saveTrack.getLocation(), saveTrack);
						else
							car.setLocation(null, null);
						return false;
					}
					// now apply schedule to car
					car.scheduleNext(track);
					// change load to ship load
					if (!car.getNextLoad().equals("")){
						car.setLoad(car.getNextLoad());
						car.setNextLoad("");
					}
					// change next wait to wait now!
					if (car.getNextWait() > 0){
						car.setWait(car.getNextWait());
						car.setNextWait(0);
					}
				}
			}
		}
		// determine if train services this car's load
		if (car.getTrain() != null){
			Train train = car.getTrain();		
			if (!train.acceptsLoad(car.getLoad(), car.getType())){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(Bundle.getMessage("carTrainNotServLoad"), new Object[]{car.getLoad(), train.getName()}),
						Bundle.getMessage("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (car.getLocation()!=null && car.getDestination()!=null && !train.servicesCar(car)){
				JOptionPane.showMessageDialog(this, 
						MessageFormat.format(Bundle.getMessage("carTrainNotService"), new Object[] {train.getName()}),
						Bundle.getMessage("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		checkTrain(car);
		// is this car part of a kernel?
		if (car.getKernel() != null){
			if (JOptionPane.showConfirmDialog(this,
					Bundle.getMessage("carInKernel"),
					Bundle.getMessage("carPartKernel"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				List<RollingStock> list = car.getKernel().getGroup();
				if (!updateGroup(list))
					return false;
			}
		}
		return true;
	}
	
	protected boolean updateGroup(List<RollingStock> list){
		for(int i=0; i<list.size(); i++){
			Car car = (Car)list.get(i);
			if (car == _car)
				continue;
			// make all cars in kernel the same
			if (!ignoreRWECheckBox.isSelected()){
				car.setReturnWhenEmptyDestination(_car.getReturnWhenEmptyDestination());
				car.setReturnWhenEmptyDestTrack(_car.getReturnWhenEmptyDestTrack());
			}
			if (!ignoreFinalDestinationCheckBox.isSelected()){
				car.setNextDestination(_car.getNextDestination());
				car.setNextDestTrack(_car.getNextDestTrack());
			}
			// update car load
			if (!ignoreLoadCheckBox.isSelected() && car.getType().equals(_car.getType()) 
					|| _car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
					|| _car.getLoad().equals(CarLoads.instance().getDefaultLoadName()))
				car.setLoad(_car.getLoad());
		}
		return super.updateGroup(list);
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		_disableComboBoxUpdate = true;	// stop updates
		super.checkBoxActionPerformed(ae);
		if (ae.getSource() == autoFinalDestTrackCheckBox) 
			updateFinalDestination();
		if (ae.getSource() == autoReturnWhenEmptyTrackCheckBox) 
			updateReturnWhenEmpty();
		if (ae.getSource() == autoTrainCheckBox) 
			updateTrainComboBox();
		if (ae.getSource() == ignoreLoadCheckBox){
			loadComboBox.setEnabled(!ignoreLoadCheckBox.isSelected());
			editLoadButton.setEnabled(!ignoreLoadCheckBox.isSelected() & _car != null);
		}
		_disableComboBoxUpdate = false;
	}
	
	protected void updateReturnWhenEmpty(){
		if (destReturnWhenEmptyBox.getSelectedItem() != null){
			if (destReturnWhenEmptyBox.getSelectedItem().equals("")){
				trackReturnWhenEmptyBox.removeAllItems();
			}else{
				log.debug("CarSetFrame sees return when empty: "+ destReturnWhenEmptyBox.getSelectedItem());
				Location l = (Location)destReturnWhenEmptyBox.getSelectedItem();
				l.updateComboBox(trackReturnWhenEmptyBox, _car, autoReturnWhenEmptyTrackCheckBox.isSelected(), true);
				if (_car != null && _car.getReturnWhenEmptyDestination() != null && _car.getReturnWhenEmptyDestination().equals(l) && _car.getReturnWhenEmptyDestTrack() != null)
					trackReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestTrack());
				packFrame();
			}
		}
	}
	
	protected void updateFinalDestination(){
		if (finalDestinationBox.getSelectedItem() != null){
			if (finalDestinationBox.getSelectedItem().equals("")){
				finalDestTrackBox.removeAllItems();
			}else{
				log.debug("CarSetFrame sees final destination: "+ finalDestinationBox.getSelectedItem());
				Location l = (Location)finalDestinationBox.getSelectedItem();
				l.updateComboBox(finalDestTrackBox, _car, autoFinalDestTrackCheckBox.isSelected(), true);
				if (_car != null && _car.getNextDestination() != null && _car.getNextDestination().equals(l) && _car.getNextDestTrack() != null)
					finalDestTrackBox.setSelectedItem(_car.getNextDestTrack());
				packFrame();
			}
		}
	}
	
	protected void updateFinalDestinationComboBoxes(){
		if (_car != null){
			log.debug("Updating final destinations for car ("+_car.toString()+")");
			finalDestinationBox.setSelectedItem(_car.getNextDestination());
		}
		updateFinalDestination();
	}
	
	protected void updateLoadComboBox(){
		if (_car != null){
			log.debug("Updating load box for car ("+_car.toString()+")");
			CarLoads.instance().updateComboBox(_car.getType(), loadComboBox);
			loadComboBox.setSelectedItem(_car.getLoad());
		}
	}
	
	protected void updateTrainComboBox(){
		if (_car != null && autoTrainCheckBox.isSelected()){
			log.debug("Updating train box for car ("+_car.toString()+")");
			trainManager.updateComboBox(trainBox, _car);
		} else {
			trainManager.updateComboBox(trainBox);
		}
		if (_car != null)
			trainBox.setSelectedItem(_car.getTrain());
	}
	
	protected void packFrame(){
		super.packFrame();
		if (getHeight()<650)
			setSize(getWidth(), 650);
	}
	
	public void dispose(){
		CarLoads.instance().removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug ("PropertyChange "+e.getPropertyName()+" "+e.getNewValue());
		if (_disableComboBoxUpdate){
			log.debug("Combobox update is disabled");
			return;
		}
		super.propertyChange(e);
		if (e.getPropertyName().equals(Car.NEXT_DESTINATION_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Car.NEXT_DESTINATION_TRACK_CHANGED_PROPERTY))
			updateFinalDestinationComboBoxes();
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Car.LOAD_CHANGED_PROPERTY)){
			updateLoadComboBox();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarSetFrame.class.getName());
}
