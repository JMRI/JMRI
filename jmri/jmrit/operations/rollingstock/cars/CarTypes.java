// CarTypes.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Represents the types of cars a railroad can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.21 $
 */
public class CarTypes {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	private static final String TYPES = rb.getString("carTypeNames"); 
		
	private static final String ARRTYPES = rb.getString("carTypeARR");
	// for property change
	public static final String CARTYPES_LENGTH_CHANGED_PROPERTY = "CarTypes Length";
	public static final String CARTYPES_NAME_CHANGED_PROPERTY = "CarTypes Name";
    
	public CarTypes() {
    }
    
	/** record the single instance **/
	private static CarTypes _instance = null;

	public static synchronized CarTypes instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarTypes creating instance");
			// create and load
			_instance = new CarTypes();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("CarTypes returns instance "+_instance);
		return _instance;
	}

    public synchronized void dispose() {
    	list.clear();
    	// remove all listeners
    	for (java.beans.PropertyChangeListener p : pcs.getPropertyChangeListeners() )
    	    pcs.removePropertyChangeListener(p);
    }
    
    protected List<String> list = new ArrayList<String>();
    
    public String[] getNames(){
     	if (list.size() == 0){
     		String[] types = TYPES.split("%%");
     		if(Setup.getCarTypes().equals(Setup.AAR))
     			types = ARRTYPES.split("%%");
     		for (int i=0; i<types.length; i++)
     			list.add(types[i]);
    	}
     	String[] types = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		types[i] = list.get(i);
   		return types;
    }
    
    public void setNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++){
 			if (!list.contains(types[i]) && !types[i].equals("Engine")){	// old code used Engine as car type remove
 				list.add(types[i]);
 			}
 		}
    }
    
    /**
     * Changes the car types from descriptive to AAR, or the other way.
     * Only removes the default car type names from the list
     */
    public void changeDefaultNames(String type){
    	if (type.equals(Setup.DESCRIPTIVE)){
    		// remove AAR types
    		String[] types = ARRTYPES.split("%%");
     		for (int i=0; i<types.length; i++)
     			list.remove(types[i]);
     		// add descriptive types
    		types = TYPES.split("%%");
    		for (int i=0; i<types.length; i++){
    			if (!list.contains(types[i]))
    				list.add(types[i]);
    		}
    	} else {
    		// remove descriptive types
    		String[] types = TYPES.split("%%");
     		for (int i=0; i<types.length; i++)
     			list.remove(types[i]);
     		// add AAR types
    		types = ARRTYPES.split("%%");
    		for (int i=0; i<types.length; i++){
    			if (!list.contains(types[i]))
    				list.add(types[i]);
    		}
     	}
    }
    
    public void addName(String type){
    	if (type == null)
    		return;
    	// insert at start of list, sort later
    	if (list.contains(type))
    		return;
    	list.add(0,type);
    	firePropertyChange (CARTYPES_LENGTH_CHANGED_PROPERTY, list.size()-1, list.size());
    }
    
    public void deleteName(String type){
    	list.remove(type);
    	firePropertyChange (CARTYPES_LENGTH_CHANGED_PROPERTY, list.size()+1, list.size());
     }
    
    public boolean containsName(String type){
    	return list.contains(type);
     }
    
    public void replaceName(String oldName, String newName){
    	addName(newName);
    	firePropertyChange (CARTYPES_NAME_CHANGED_PROPERTY, oldName, newName);
    	// need to keep old name so location manager can replace properly
       	deleteName(oldName);
    }
    
    public JComboBox getComboBox(){
    	JComboBox box = new JComboBox();
		String[] types = getNames();
		for (int i = 0; i < types.length; i++)
			box.addItem(types[i]);
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		String[] types = getNames();
		for (int i = 0; i < types.length; i++)
			box.addItem(types[i]);
    }
 
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarTypes.class.getName());

}

