// Consist.java

package jmri.jmrit.operations.rollingstock.engines;
import org.apache.log4j.Logger;
import java.util.*;

import jmri.jmrit.operations.rollingstock.RollingStockGroup;

/**
 * A consist is a group of engines that is managed as one engine
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2010
 * @version $Revision$
 */
public class Consist extends RollingStockGroup{
	
	protected int _consistNumber = 0;
	
	public Consist(String name){
		super(name);
		log.debug("New Consist (" + name +")");
	}

	public List<Engine> getEngines(){
		List<Engine> engines = new ArrayList<Engine>();
		for (int i=0; i<getGroup().size(); i++){
			engines.add((Engine)getGroup().get(i));
		}
		return engines;
	}

	public int getConsistNumber(){
		return _consistNumber;
	}
	
	/**
	 * 
	 * @param number DCC address for this consist
	 */
	public void setConsistNumber(int number){
		_consistNumber = number;
	}
	
	public void dispose(){
		while (getGroup().size()>0){
			Engine engine = (Engine)getGroup().get(0);
			if (engine != null){
				engine.setConsist(null);
			}
		}
		super.dispose();
	}

	static Logger log = org.apache.log4j.Logger
	.getLogger(Consist.class.getName());
}
