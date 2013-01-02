// CarOwners.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the owner names that cars can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision$
 */
public class CarOwners {
	
	public static final String CAROWNERS_NAME_CHANGED_PROPERTY = "CarOwners Name"; // NOI18N
	public static final String CAROWNERS_LENGTH_CHANGED_PROPERTY = "CarOwners Length"; // NOI18N
	
	private static final int MIN_NAME_LENGTH = 4;
	
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
    	maxNameLength = 0;	// reset maximum name length
    	firePropertyChange (CAROWNERS_LENGTH_CHANGED_PROPERTY, list.size()-1, list.size());
    }
    
    public void deleteName(String owner){
    	list.remove(owner);
    	maxNameLength = 0;	// reset maximum name length
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
    
    private int maxNameLength = 0;
    
    public int getCurMaxNameLength(){
    	if (maxNameLength == 0){
    		String[] colors = getNames();
    		int length = MIN_NAME_LENGTH;
    		for (int i = 0; i < colors.length; i++){
    			if (colors[i].length()>length)
    				length = colors[i].length();
    		}
    		maxNameLength = length;
    		return length;
    	} else {
    		return maxNameLength;
    	}
    }
        
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    protected void firePropertyChange(String p, Object old, Object n) {
    	// Set dirty
    	CarManagerXml.instance().setDirty(true);
    	pcs.firePropertyChange(p,old,n);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarOwners.class.getName());

}

