// Consist.java

package jmri.jmrit.operations.engines;
import java.util.*;

public class Consist {
	
	protected String _name ="";
	protected int _length = 0;
	protected Engine _leadEngine = null;
	
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
	
	List _engines = new ArrayList();
	
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
		firePropertyChange("listLength", Integer.toString(oldSize), new Integer(_engines.size()));
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
			setLeadEngine((Engine)_engines.get(0));
		}
		firePropertyChange("listLength", Integer.toString(oldSize), new Integer(_engines.size()));
	}
	
	public List getEngines(){
		return _engines;
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
	
	public boolean isLeadEngine(Engine engine){
		if(engine == _leadEngine)
			return true;
		else
			return false;
	}
	
	public void setLeadEngine(Engine engine){
		_leadEngine = engine;
	}
	
	public void dispose(){
		while (_engines.size()>0){
			Engine engine = (Engine)_engines.get(0);
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

	
	static org.apache.log4j.Category log = org.apache.log4j.Category
	.getInstance(Consist.class.getName());
}