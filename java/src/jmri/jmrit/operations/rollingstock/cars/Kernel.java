// Kernel.java

package jmri.jmrit.operations.rollingstock.cars;
import org.apache.log4j.Logger;
import java.util.*;

import jmri.jmrit.operations.rollingstock.RollingStockGroup;

/**
 * A Kernel is a group of cars that is managed as one car.
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version	$Revision$
 */
public class Kernel extends RollingStockGroup{
	
	public Kernel(String name){
		super(name);
		log.debug("New Kernel (" + name +")");
	}
	
	public List<Car> getCars(){
		List<Car> cars = new ArrayList<Car>();
		for (int i=0; i<getGroup().size(); i++){
			cars.add((Car)getGroup().get(i));
		}
		return cars;
	}
	
	public void dispose(){
		while (getGroup().size()>0){
			Car car = (Car)getGroup().get(0);
			if (car != null){
				car.setKernel(null);
			}
		}
		super.dispose();
	}
	
	static Logger log = org.apache.log4j.Logger
	.getLogger(Kernel.class.getName());
}
