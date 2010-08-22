// CarOwners.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the owner names that cars can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.11 $
 */
public class CarOwners {
	
	public static final String CAROWNERS_NAME_CHANGED_PROPERTY = "CarOwners Name";
	public static final String CAROWNERS_LENGTH_CHANGED_PROPERTY = "CarOwners Length";
	
    public CarOwners() {
    }
    
	/** record the single instance **/
	private static CarOwners _instance = null;

	public static synchronized CarOwners instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("CarOwners creating instance");
			// create and load
			_instance = new CarOwners();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("CarOwners returns instance "+_instance);
		return _instance;
	}

    public synchronized void dispose() {
    	list.clear();
    	 	
    	// remove all listeners
    	for (java.beans.PropertyChangeListener p : pcs.getPropertyChangeListeners() )
    	    pcs.removePropertyChangeListener(p);
        
        _instance = null;
    }

    List<String> list = new ArrayList<String>();
    
    public String[] getNames(){
     	String[] owners = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		owners[i] = list.get(i);
   		return owners;
    }
    
    public void setNames(String[] owners){
    	if (owners.length == 0) return;
    	jmri.util.StringUtil.sort(owners);
 		for (int i=0; i<owners.length; i++)
 			if (!list.contains(owners[i]))
 				list.add(owners[i]);
    }
    
    public void addName(String owner){
    	// insert at start of list, sort later
    	if (list.contains(owner))
    		return;
    	list.add(0,owner);
    	firePropertyChange (CAROWNERS_LENGTH_CHANGED_PROPERTY, list.size()-1, list.size());
    }
    
    public void deleteName(String owner){
    	list.remove(owner);
    	firePropertyChange (CAROWNERS_LENGTH_CHANGED_PROPERTY, list.size()+1, list.size());
    }
    
    public void replaceName(String oldName, String newName){
    	addName(newName);
    	list.remove(oldName);
    	firePropertyChange (CAROWNERS_NAME_CHANGED_PROPERTY, oldName, newName);
    	if (newName == null)
    		firePropertyChange (CAROWNERS_LENGTH_CHANGED_PROPERTY, list.size()+1, list.size());
    }
    
    public boolean containsName(String owner){
    	return list.contains(owner);
     }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
		String[] owners = getNames();
		for (int i = 0; i < owners.length; i++)
			box.addItem(owners[i]);
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		String[] owners = getNames();
		for (int i = 0; i < owners.length; i++)
			box.addItem(owners[i]);
    }
        
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarOwners.class.getName());

}

