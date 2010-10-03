// CarLoads.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;

import org.jdom.Element;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the loads that cars can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.14 $
 */
public class CarLoads {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	protected Hashtable<String, List<CarLoad>> list = new Hashtable<String, List<CarLoad>>();
	protected String _emptyName = rb.getString("EmptyCar");	
	protected String _loadName = rb.getString("LoadedCar");
	
	// for property change
	public static final String LOAD_CHANGED_PROPERTY = "CarLoads Load";
	public static final String LOAD_NAME_CHANGED_PROPERTY = "CarLoads Name";
    
	public CarLoads() {
    }
    
	/** record the single instance **/
	private static CarLoads _instance = null;

	public static synchronized CarLoads instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarLoads creating instance");
			// create and load
			_instance = new CarLoads();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("CarLoads returns instance "+_instance);
		return _instance;
	}

    public synchronized void dispose() {
    	list.clear(); 	
    	// remove all listeners
    	for (java.beans.PropertyChangeListener p : pcs.getPropertyChangeListeners() )
    	    pcs.removePropertyChangeListener(p);
    }
    
    /**
     * Add a car type with specific loads
     * @param type car type
     */
    public void addType(String type){
    	list.put(type, new ArrayList<CarLoad>());
    }
    
    public void replaceType(String oldType, String newType){
    	List<String> names = getNames(oldType);
    	addType(newType);
    	for (int i=0; i<names.size(); i++){
    		addName(newType, names.get(i));
    	}
    	list.remove(oldType);
    }
    
    /**
     * Gets the appropriate car loads for the car's type.
     * @param type
     * @return JComboBox with car loads starting with empty string.
     */
    public JComboBox getSelectComboBox(String type){
    	JComboBox box = new JComboBox();
    	box.addItem("");
		List<String> loads = getNames(type);
		for (int i=0; i<loads.size(); i++){
			box.addItem(loads.get(i));
		}
    	return box;
    }
    
    /**
     * Gets the appropriate car loads for the car's type.
     * @param type
     * @return JComboBox with car loads.
     */
    public JComboBox getComboBox(String type){
    	JComboBox box = new JComboBox();
    	List<String> loads = getNames(type);
    	for (int i=0; i<loads.size(); i++){
    		box.addItem(loads.get(i));
    	}
    	return box;
    }
    
    /**
     * Gets a combobox with the available priorities
     * @return JComboBox with car priorities.
     */
    public JComboBox getPriorityComboBox(){
    	JComboBox box = new JComboBox();
    	box.addItem(CarLoad.PRIORITY_LOW);
    	box.addItem(CarLoad.PRIORITY_HIGH);
    	return box;
    }
    
    public List<String> getNames(String type){
    	if (type == null){
    		List<String> names = new ArrayList<String>();
    		names.add(getDefaultEmptyName());
    		names.add(getDefaultLoadName());
    		return names;
    	}
    	List<CarLoad> loads = list.get(type);
    	if (loads == null){
    		addType(type);
    		loads = list.get(type);
    	}
    	if (loads.size() == 0){
    		loads.add(new CarLoad(getDefaultEmptyName()));
    		loads.add(new CarLoad(getDefaultLoadName()));
    	}
    	List<String> names = new ArrayList<String>();
    	for (int i=0; i<loads.size(); i++){
    		names.add(loads.get(i).getName());
    	}
    	return names;
    }

    public void addName(String type, String name){
    	List<CarLoad> loads = list.get(type);
    	if (loads == null){
    		log.debug("car type ("+type+") does not exist");
    		return;
    	}
    	// don't add if name already exists
    	if (containsName(type, name))
    		return;
    	loads.add(0, new CarLoad(name));
    	firePropertyChange (LOAD_CHANGED_PROPERTY, loads.size()-1, loads.size());
    }

    public void deleteName(String type, String name){
    	List<CarLoad> loads = list.get(type);
    	if (loads == null){
    		log.debug("car type ("+type+") does not exist");
    		return;
    	}
    	for (int i=0; i<loads.size(); i++){
    		CarLoad cl = loads.get(i);
    		if (cl.getName().equals(name)){
    			loads.remove(i);
    			break;
    		}
    	}
    	firePropertyChange (LOAD_CHANGED_PROPERTY, loads.size()+1, loads.size());
    }
    
    public boolean containsName(String type, String name){
       	List<CarLoad> loads = list.get(type);
    	if (loads == null){
    		//log.debug("car type ("+type+") does not exist");
    		return false;
    	}
       	for (int i=0; i<loads.size(); i++){
    		CarLoad cl = loads.get(i);
    		if (cl.getName().equals(name)){
    			return true;
    		}
    	}
    	return false;
    }
    
    public void updateComboBox(String type, JComboBox box) {
    	box.removeAllItems();
   		List<String> loads = getNames(type);
		for (int i=0; i<loads.size(); i++){
			box.addItem(loads.get(i));
		}
    }
    
    public void replaceName(String type, String oldName, String newName){
    	addName(type, newName);
    	deleteName(type, oldName);
    	firePropertyChange (LOAD_NAME_CHANGED_PROPERTY, oldName, newName);
    }
    
    public String getDefaultLoadName(){
     	return _loadName;
    }
    
    public void setDefaultLoadName(String name){
    	String old = _loadName;
    	_loadName = name;
    	firePropertyChange (LOAD_NAME_CHANGED_PROPERTY, old, name);
    }
    
    public String getDefaultEmptyName(){
    	return _emptyName;
    }
    
    public void setDefaultEmptyName(String name){
    	String old = _emptyName;
    	_emptyName = name;
    	firePropertyChange (LOAD_NAME_CHANGED_PROPERTY, old, name);
    }
    
    public void setPriority(String type, String name, String priority){
    	List<CarLoad> loads = list.get(type);
       	for (int i=0; i<loads.size(); i++){
    		CarLoad cl = loads.get(i);
    		if (cl.getName().equals(name))
    			cl.setPriority(priority);
       	}
    }
    
    public String getPriority(String type, String name){
       	if (!containsName(type, name))
    		return CarLoad.PRIORITY_LOW;
    	List<CarLoad> loads = list.get(type);
       	for (int i=0; i<loads.size(); i++){
    		CarLoad cl = loads.get(i);
    		if (cl.getName().equals(name))
    			return cl.getPriority();
       	}
       	return "error";
    }
    
    public void setPickupComment(String type, String name, String comment){
    	if (!containsName(type, name))
    		return;
    	List<CarLoad> loads = list.get(type);
       	for (int i=0; i<loads.size(); i++){
    		CarLoad cl = loads.get(i);
    		if (cl.getName().equals(name))
    			cl.setPickupComment(comment);
       	}
    }
    
    public String getPickupComment(String type, String name){
    	if (!containsName(type, name))
    		return "";
    	List<CarLoad> loads = list.get(type);
       	for (int i=0; i<loads.size(); i++){
    		CarLoad cl = loads.get(i);
    		if (cl.getName().equals(name))
    			return cl.getPickupComment();
       	}
       	return "";
    }
    
    public void setDropComment(String type, String name, String comment){
    	if (!containsName(type, name))
    		return;
    	List<CarLoad> loads = list.get(type);
       	for (int i=0; i<loads.size(); i++){
    		CarLoad cl = loads.get(i);
    		if (cl.getName().equals(name))
    			cl.setDropComment(comment);
       	}
    }
    
    public String getDropComment(String type, String name){
    	if (!containsName(type, name))
    		return "";
    	List<CarLoad> loads = list.get(type);
       	for (int i=0; i<loads.size(); i++){
    		CarLoad cl = loads.get(i);
    		if (cl.getName().equals(name))
    			return cl.getDropComment();
       	}
       	return "";
    }
    
	/**
	 * Create an XML element to represent this Entry. This member has to remain
	 * synchronized with the detailed DTD in operations-cars.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public Element store() {
		Element values = new Element("loads");
		// store default load and empty
		Element defaults = new Element("defaults");
		defaults.setAttribute("empty", getDefaultEmptyName());
		defaults.setAttribute("load", getDefaultLoadName());
		values.addContent(defaults);
		// store loads based on car types
		Enumeration<String> en = list.keys();
		while(en.hasMoreElements()) {
			String key = en.nextElement();
			Element load = new Element("load");
			load.setAttribute("type", key);
			List<CarLoad> loads = list.get(key);
			StringBuffer buf = new StringBuffer();
			for (int j=0; j<loads.size(); j++){
				buf.append(loads.get(j).getName());
				Element carLoad = new Element("carLoad");
				carLoad.setAttribute("name", loads.get(j).getName());
				if (!loads.get(j).getPriority().equals(CarLoad.PRIORITY_LOW)){
					carLoad.setAttribute("priority", loads.get(j).getPriority());
					buf.append("P");	// must store
				}
				if (!loads.get(j).getPickupComment().equals("")){
					carLoad.setAttribute("pickupComment", loads.get(j).getPickupComment());
					buf.append("PC");	// must store
				}
				if (!loads.get(j).getDropComment().equals("")){
					carLoad.setAttribute("dropComment", loads.get(j).getDropComment());
					buf.append("DC");	// must store
				}
				load.addContent(carLoad);
			}
			// only store loads that aren't the defaults
			if(!buf.toString().equals(getDefaultEmptyName()+getDefaultLoadName()))
				values.addContent(load);
		}
		return values;
	}
	
	public void load(Element e){
		org.jdom.Attribute a;
		Element defaults = e.getChild("loads").getChild("defaults");
		if (defaults != null){
			if((a = defaults.getAttribute("load")) != null){
				_loadName = a.getValue();
			}
			if((a = defaults.getAttribute("empty")) != null){
				_emptyName = a.getValue();
			}
		}
		@SuppressWarnings("unchecked")
        List<Element> l = e.getChild("loads").getChildren("load");
        if (log.isDebugEnabled()) log.debug("readFile sees "+l.size()+" car loads");
        for (int i=0; i<l.size(); i++) {
        	Element load = l.get(i);
        	if((a = load.getAttribute("type")) != null){
        		String type = a.getValue();
        		addType(type);
        		// old style had a list of names
        		if((a = load.getAttribute("names")) != null){
        			String names = a.getValue();
        			String[] loadNames = names.split("%%");
        			jmri.util.StringUtil.sort(loadNames);
        			if (log.isDebugEnabled()) log.debug("Car load type: "+type+" loads: "+names);
        			// addName puts new items at the start, so reverse load
        			for (int j=loadNames.length; j>0;){
        				addName(type, loadNames[--j]);
        			}
        		}
        		// new style load and comments
        		@SuppressWarnings("unchecked")
        		List<Element> loads = load.getChildren("carLoad");
        		if (log.isDebugEnabled()) log.debug(loads.size()+" car loads for type: "+type);
        		for (int j=0; j<loads.size(); j++){
        			Element carLoad = loads.get(j);      			
        			if ((a = carLoad.getAttribute("name")) != null){
        				String name = a.getValue();
        				addName(type, name);
        				if ((a = carLoad.getAttribute("priority")) != null){
        					setPriority(type, name, a.getValue());
        				}
        				if ((a = carLoad.getAttribute("pickupComment")) != null){
        					setPickupComment(type, name, a.getValue());
        				}
           				if ((a = carLoad.getAttribute("dropComment")) != null){
        					setDropComment(type, name, a.getValue());
        				}
        			}
        		}
        	}
        }
	}
        
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarLoads.class.getName());

}

