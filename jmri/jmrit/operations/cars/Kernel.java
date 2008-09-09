// Kernel.java

package jmri.jmrit.operations.cars;
import java.util.*;

public class Kernel {
	
	protected String _name ="";
	protected int _length = 0;
	protected double _weight = 0;
	protected int _weightTons = 0;
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
	
	List _cars = new ArrayList();
	
	public void addCar(Car car){
		if(_cars.size() <= 0){
			_leadCar = car;
		}
		int oldSize = _cars.size();
		setLength(getLength()+ Integer.parseInt(car.getLength()) + Car.COUPLER);
		try {
			setWeight(getWeight()+ Double.parseDouble(car.getWeight()));
			setWeightTons(getWeightTons()+ Integer.parseInt(car.getWeightTons()));
		} catch (Exception e){
			log.debug ("car ("+car.getId()+") weight not set");
		}
		_cars.add(car);
		firePropertyChange("listLength", Integer.toString(oldSize), new Integer(_cars.size()));
	}
	
	public void deleteCar(Car car){
		int oldSize = _cars.size();
		setLength(getLength()- (Integer.parseInt(car.getLength()) + Car.COUPLER));
		setWeight(getWeight()- Double.parseDouble(car.getWeight()));
		setWeightTons(getWeightTons()- Integer.parseInt(car.getWeightTons()));
		_cars.remove(car);
		if(isLeadCar(car) && _cars.size()>0){
			// need a new lead car
			setLeadCar((Car)_cars.get(0));
		}
		firePropertyChange("listLength", Integer.toString(oldSize), new Integer(_cars.size()));
	}
	
	public List getCars(){
		return _cars;
	}
	
	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			firePropertyChange("kernel length", Integer.toString(old), Integer.toString(length));
	}

	public int getLength() {
		return _length;
	}
	
	public void setWeight(double weight){
		_weight = weight;
	}
	
	public double getWeight() {
		return _weight;
	}
	
	public void setWeightTons(int weight){
		_weightTons = weight;
	}
	
	public int getWeightTons() {
		return _weightTons;
	}
	
	public boolean isLeadCar(Car car){
		if(car == _leadCar)
			return true;
		else
			return false;
	}
	
	public void setLeadCar(Car car){
		_leadCar = car;
	}
	
	public void dispose(){
		while (_cars.size()>0){
			Car car = (Car)_cars.get(0);
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

	
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(Kernel.class.getName());
}