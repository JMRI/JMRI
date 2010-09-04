// Consist.java

package jmri.jmrit.operations.rollingstock.engines;
import java.util.*;

/**
 * A consist is a group of engines that is managed as one engine
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.9 $
 */
public class Consist {
	
	protected String _name ="";
	protected int _length = 0;
	protected Engine _leadEngine = null;
	protected int _consistNumber = 0;
	
	public Consist(String name){
		_name = name;
		log.debug("New Consist (" + name +")");
	}
	
	public String getName(){
		return _name;
	}
	
	// for combo boxes
	public String toString(){
		return _name;
	}
	
	List<Engine> _engines = new ArrayList<Engine>();
	
	public void addEngine(Engine engine){
		if (_engines.contains(engine)){
			log.debug("engine "+engine.getId()+" alreay part of consist "+getName());
			return;
		}
		if(_engines.size() <= 0){
			_leadEngine = engine;
		}
		int oldSize = _engines.size();
		setLength(getLength()+ Integer.parseInt(engine.getLength()) + Engine.COUPLER);
		_engines.add(engine);
		firePropertyChange("listLength", Integer.toString(oldSize), Integer.valueOf(_engines.size()));
	}
	
	public void deleteEngine(Engine engine){
		if (!_engines.contains(engine)){
			log.debug("engine "+engine.getId()+" not part of consist "+getName());
			return;
		}
		int oldSize = _engines.size();
		setLength(getLength()- (Integer.parseInt(engine.getLength()) + Engine.COUPLER));
		_engines.remove(engine);
		if(isLeadEngine(engine) && _engines.size()>0){
			// need a new lead engine
			setLeadEngine(_engines.get(0));
		}
		firePropertyChange("listLength", Integer.toString(oldSize), Integer.valueOf(_engines.size()));
	}
	
	public List<Engine> getEngines(){
		return _engines;
	}
	
	public void setLength(int length) {
		int old = _length;
		_length = length;
		if (old != length)
			firePropertyChange("consist length", Integer.toString(old), Integer.toString(length));
	}

	public int getLength() {
		return _length;
	}
	
	public boolean isLeadEngine(Engine engine){
		if(engine == _leadEngine)
			return true;
		return false;
	}
	
	/**
	 * Sets the lead engine for this consist. Engine must be already assigned to
	 * this consist.
	 * 
	 * @see #addEngine(Engine engine)
	 * 
	 * @param engine
	 *            lead engine for this consist
	 */
	public void setLeadEngine(Engine engine){
		if (_engines.contains(engine)){
			_leadEngine = engine;
		}
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
		while (_engines.size()>0){
			Engine engine = _engines.get(0);
			if (engine != null){
				engine.setConsist(null);
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
	.getLogger(Consist.class.getName());
}