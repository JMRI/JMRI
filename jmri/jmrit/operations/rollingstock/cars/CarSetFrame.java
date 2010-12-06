// CarSetFrame.java

package jmri.jmrit.operations.rollingstock.cars;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

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
 * @version $Revision: 1.24 $
 */

public class CarSetFrame extends RollingStockSetFrame implements java.beans.PropertyChangeListener {

	protected static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	CarManagerXml managerXml = CarManagerXml.instance();
	
	Car _car;
		
	public CarSetFrame() {
		super();
	}

	public void initComponents() {
		super.initComponents();

		// build menu
		addHelpMenu("package.jmri.jmrit.operations.Operations_CarsSet", true);
		
		// Only show returnWhenEmpty and nextDestination if routing enabled
		pOptionalrwe.setVisible(Setup.isCarRoutingEnabled());
		pFinalDestination.setVisible(Setup.isCarRoutingEnabled());
		
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
	
	protected boolean save(){
		if (!super.save())
			return false;
		// return when empty fields
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
		// final destination fields
		if (finalDestinationBox.getSelectedItem() == null || finalDestinationBox.getSelectedItem().equals("")) {
			_car.setNextDestination(null);
			_car.setNextDestTrack(null);
		} else {
			if (finalDestTrackBox.getSelectedItem() != null 
					&& !finalDestTrackBox.getSelectedItem().equals("")){
				_car.setNextDestTrack((Track)finalDestTrackBox.getSelectedItem());
			} else {
				_car.setNextDestTrack(null);
			}
			_car.setNextDestination((Location) finalDestinationBox.getSelectedItem());
		}
		if (_car.getTrain() != null){
			Train train = _car.getTrain();
			// determine if train services this car's load
			if (!train.acceptsLoadName(_car.getLoad())){
				JOptionPane.showMessageDialog(this,
						MessageFormat.format(rb.getString("carTrainNotServLoad"), new Object[]{_car.getLoad(), train.getName()}),
						rb.getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			if (_car.getDestination()!=null && !train.servicesCar(_car)){
				JOptionPane.showMessageDialog(this, MessageFormat.format(rb.getString("carTrainNotService"),
						new Object[] {train.getName()}), rb.getString("rsNotMove"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		// is this car part of a kernel?
		if (_car.getKernel() != null){
			if (JOptionPane.showConfirmDialog(this,
					rb.getString("carInKernel"),
					rb.getString("carPartKernel"),
					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
				// convert cars list to rolling stock list
				List<Car> cars = _car.getKernel().getCars();
				List<RollingStock> list = new ArrayList<RollingStock>();
				for (int i=0; i<cars.size(); i++)
					list.add(cars.get(i));
				if (!updateGroup(list))
					return false;
			}
		}
		managerXml.writeOperationsFile();
		return true;
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
				if (_car.getReturnWhenEmptyDestination() != null && _car.getReturnWhenEmptyDestination().equals(l) && _car.getReturnWhenEmptyDestTrack() != null)
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
				if (_car.getNextDestination() != null && _car.getNextDestination().equals(l) && _car.getNextDestTrack() != null)
					finalDestTrackBox.setSelectedItem(_car.getNextDestTrack());
				packFrame();
			}
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(CarSetFrame.class.getName());
}
