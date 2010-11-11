// Kernel.java

package jmri.jmrit.operations.rollingstock.cars;
import java.util.*;

/**
 * A Kernel is a group of cars that is managed as one car.
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.8 $
 */
public class Kernel {
	
	protected String _name ="";
	protected double _weight = 0;
	protected Car _leadCar = null;
	
	public Kernel(String name){
		_name = name;
		log.debug("New Kernel (" + name +")");
	}
	
	public String getName(){
		return _name;
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}
	
	List<Car> _cars = new ArrayList<Car>();
	
	public void addCar(Car car){
		if (_cars.contains(car)){
			log.debug("car "+car.getId()+" alreay part of kernel "+getName());
			return;
		}
		if(_cars.size() <= 0){
			_leadCar = car;
		}
		int oldSize = _cars.size();
		try {
			setWeight(getWeight()+ Double.parseDouble(car.getWeight()));
		} catch (Exception e){
			log.debug ("car ("+car.getId()+") weight not set");
		}
		_cars.add(car);
		firePropertyChange("listLength", Integer.toString(oldSize), Integer.valueOf(_cars.size()));
	}
	
	public void deleteCar(Car car){
		if (!_cars.contains(car)){
			log.debug("car "+car.getId()+" not part of kernel "+getName());
			return;
		}
		int oldSize = _cars.size();
		setWeight(getWeight()- Double.parseDouble(car.getWeight()));
		_cars.remove(car);
		if(isLeadCar(car) && _cars.size()>0){
			// need a new lead car
			setLeadCar(_cars.get(0));
		}
		firePropertyChange("listLength", Integer.toString(oldSize), Integer.valueOf(_cars.size()));
	}
	
	public List<Car> getCars(){
		return _cars;
	}

	public int getLength() {
		int length = 0;
		for (int i=0; i<_cars.size(); i++){
			Car car = _cars.get(i);
			length = length + Integer.parseInt(car.getLength()) + Car.COUPLER;
		}
		return length;
	}
	
	public void setWeight(double weight){
		_weight = weight;
	}
	
	public double getWeight() {
		return _weight;
	}
	
	/**
	 * Get a kernel's weight adjusted for car loads
	 * @return kernel's weight
	 */
	public int getAdjustedWeightTons() {
		int weightTons = 0;
		for (int i=0; i<_cars.size(); i++){
			Car car = _cars.get(i);
			weightTons = weightTons + car.getAdjustedWeightTons();
		}
		return weightTons;
	}
	
	public boolean isLeadCar(Car car){
		if(car == _leadCar)
			return true;
		return false;
	}
	
	/**
	 * Sets the lead car for this kernel. Car must be part of the kernel. The
	 * groups of cars that make up this kernel will have the attributes of the
	 * lead car. However, the length attribute is the sum of all cars lengths
	 * plus the coupler lengths.
	 * 
	 * @param car
	 *            lead car for this kernel.
	 */
	public void setLeadCar(Car car){
		if (_cars.contains(car)){
			_leadCar = car;
		}
	}
	
	public void dispose(){
		while (_cars.size()>0){
			Car car = _cars.get(0);
			if (car != null){
				car.setKernel(null);
			}
		}
	}
	
	
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(
			this);

	public synchronized void addPropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(
			java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		pcs.firePropertyChange(p, old, n);
	}

	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(Kernel.class.getName());
}