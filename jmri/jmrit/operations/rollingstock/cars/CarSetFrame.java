// CarSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockSetFrame;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;


/**
 * Frame for user to place car on the layout
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2010
 * @version $Revision: 1.29 $
 */

public class CarSetFrame extends RollingStockSetFrame implements java.beans.PropertyChangeListener {

	protected static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	
	Car _car;
	
	// combo boxes
	JComboBox loadComboBox = CarLoads.instance().getComboBox(null);
	
	// buttons
	JButton editLoadButton = new JButton(rb.getString("Edit"));	
	
	CarLoadEditFrame lef = null;
		
	public CarSetFrame() {
		super();
	}

	public void initComponents() {
		super.initComponents();

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_CarsSet", true);
		
		// Only show nextDestination if routing enabled
		pOptional.setVisible(true);
		pFinalDestination.setVisible(Setup.isCarRoutingEnabled());
		
		// add load fields
		JPanel pLoad = new JPanel();
		pLoad.setLayout(new GridBagLayout());
		pLoad.setBorder(BorderFactory.createTitledBorder(rb.getString("Load")));
		addItem(pLoad, loadComboBox, 1, 0);
		addItem(pLoad, editLoadButton, 2, 0);
		pOptional.add(pLoad);
		
		// setup combobox
		addComboBoxAction(loadComboBox);
		
		// setup button
		addButtonAction(editLoadButton);
		
		// get notified if combo box gets modified
		CarLoads.instance().addPropertyChangeListener(this);
		
		packFrame();
	}
	
	public void loadCar(Car car){
		_car = car;
		load(car);
	}
	
	protected void updateComboBoxes(){
		if (_disableComboBoxUpdate)
			return;
		super.updateComboBoxes();
		finalDestinationBox.setSelectedItem(_car.getNextDestination());
		destReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestination());
		
		updateFinalDestination();
		updateReturnWhenEmpty();
		updateLoadComboBox();
	}
	
	protected void enableComponents(boolean enabled){
		super.enableComponents(enabled);
		loadComboBox.setEnabled(enabled);
		editLoadButton.setEnabled(enabled & _car != null);
	}
	
	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		super.comboBoxActionPerformed(ae);
		if (ae.getSource()== finalDestinationBox){
			updateFinalDestination();
		}
		if (ae.getSource()== destReturnWhenEmptyBox){
			updateReturnWhenEmpty();
		}
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
			managerXml.writeOperationsFile();
			return true;
		}
		return false;
	}
	
	protected boolean change(Car car){
		// set final destination fields before destination in case there's a schedule at destination
		if (finalDestinationBox.getSelectedItem() == null || finalDestinationBox.getSelectedItem().equals("")) {
			car.setNextDestination(null);
			car.setNextDestTrack(null);
		} else {
			Track finalDestTrack = null;
			if (finalDestTrackBox.getSelectedItem() != null 
					&& !finalDestTrackBox.getSelectedItem().equals(""))
				finalDestTrack = (Track)finalDestTrackBox.getSelectedItem();
			String status = car.testDestination((Location) finalDestinationBox.getSelectedItem(), finalDestTrack);
			if (!status.equals(Car.OKAY)){
				JOptionPane.showMessageDialog(this,
						getRb().getString("rsCanNotFinalMsg")+ status,
						getRb().getString("rsCanNotFinal"),
						JOptionPane.WARNING_MESSAGE);
			}
			car.setNextDestination((Location) finalDestinationBox.getSelectedItem());
			car.setNextDestTrack(finalDestTrack);
		}
		if (!super.change(car))
			return false;
		// return when empty fields
		if (destReturnWhenEmptyBox.getSelectedItem() == null || destReturnWhenEmptyBox.getSelectedItem().equals("")) {
			car.setReturnWhenEmptyDestination(null);
			car.setReturnWhenEmptyDestTrack(null);
		} else {
			if (trackReturnWhenEmptyBox.getSelectedItem() != null 
					&& !trackReturnWhenEmptyBox.getSelectedItem().equals("")){	
				String status = car.testDestination((Location) destReturnWhenEmptyBox.getSelectedItem(), (Track)trackReturnWhenEmptyBox.getSelectedItem());
				if (!status.equals(Car.OKAY)){
					JOptionPane.showMessageDialog(this,
							getRb().getString("rsCanNotRWEMsg")+ status,
							getRb().getString("rsCanNotRWE"),
							JOptionPane.WARNING_MESSAGE);
				}
				car.setReturnWhenEmptyDestTrack((Track)trackReturnWhenEmptyBox.getSelectedItem());
			} else {
				car.setReturnWhenEmptyDestTrack(null);
			}
			car.setReturnWhenEmptyDestination((Location) destReturnWhenEmptyBox.getSelectedItem());
		}
		// car load
		if (loadComboBox.getSelectedItem() != null){
			car.setLoad((String)loadComboBox.getSelectedItem());
		}
		if (car.getTrain() != null){
			Train train = car.getTrain();
			// determine if train services this car's load
			if (!train.acceptsLoadName(car.getLoad())){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(rb.getString("carTrainNotServLoad"), new Object[]{car.getLoad(), train.getName()}),
						rb.getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (car.getDestination()!=null && !train.servicesCar(car)){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carTrainNotService"),
						new Object[] {train.getName()}), rb.getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// is this car part of a kernel?
		if (car.getKernel() != null){
			if (JOptionPane.showConfirmDialog(this,
					rb.getString("carInKernel"),
					rb.getString("carPartKernel"),
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
			car.setReturnWhenEmptyDestination(_car.getReturnWhenEmptyDestination());
			car.setReturnWhenEmptyDestTrack(_car.getReturnWhenEmptyDestTrack());
			car.setNextDestination(_car.getNextDestination());
			car.setNextDestTrack(_car.getNextDestTrack());
			// update car load
			if (car.getType().equals(_car.getType()) 
					|| _car.getLoad().equals(CarLoads.instance().getDefaultEmptyName())
					|| _car.getLoad().equals(CarLoads.instance().getDefaultLoadName()))
				car.setLoad(_car.getLoad());
		}
		return super.updateGroup(list);
	}
	
	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		super.checkBoxActionPerformed(ae);
		if (ae.getSource() == autoFinalDestTrackCheckBox) 
			updateFinalDestination();
		if (ae.getSource() == autoReturnWhenEmptyTrackCheckBox) 
			updateReturnWhenEmpty();
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
				log.debug("CarSetFrame sees destination: "+ finalDestinationBox.getSelectedItem());
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
	
	protected void packFrame(){
		super.packFrame();
		if (getHeight()<600)
			setSize(getWidth(), 600);
	}
	
	public void dispose(){
		CarLoads.instance().removePropertyChangeListener(this);
		super.dispose();
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		super.propertyChange(e);
		if (e.getPropertyName().equals(Car.NEXT_DESTINATION_CHANGED_PROPERTY) ||
				e.getPropertyName().equals(Car.NEXT_DESTINATION_TRACK_CHANGED_PROPERTY))
			updateFinalDestinationComboBoxes();
		if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)){
			updateLoadComboBox();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarSetFrame.class.getName());
}
