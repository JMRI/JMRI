// CarOwners.java

package jmri.jmrit.operations.rollingstock.cars;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

import org.jdom.Attribute;
import org.jdom.Element;

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
    
	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-cars.dtd.
	 * 
	 */
	public void store(Element root) {       
        String[]names = getNames();
        if (Control.backwardCompatible) {
        	Element values = new Element(Xml.CAR_OWNERS);
        	for (int i=0; i<names.length; i++){
        		String ownerNames = names[i]+"%%"; // NOI18N
        		values.addContent(ownerNames);
        	}
        	root.addContent(values);
        }
        // new format using elements
        Element owners = new Element(Xml.OWNERS);
        for (int i=0; i<names.length; i++){
        	Element owner = new Element(Xml.OWNER);
        	owner.setAttribute(new Attribute(Xml.NAME, names[i]));
        	owners.addContent(owner);
        }
        root.addContent(owners);
	}
	
	public void load(Element root) {
		// new format using elements starting version 3.3.1
		if (root.getChild(Xml.OWNERS)!= null){
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.OWNERS).getChildren(Xml.OWNER);
			if (log.isDebugEnabled()) log.debug("Car owners sees "+l.size()+" owners");
			Attribute a;
			String[] owners = new String[l.size()];
			for (int i=0; i<l.size(); i++) {
				Element owner = l.get(i);
				if ((a = owner.getAttribute(Xml.NAME)) != null) {
					owners[i] = a.getValue();
				}
			}
			setNames(owners);
		}
		// old format
		else if (root.getChild(Xml.CAR_OWNERS)!= null){
        	String names = root.getChildText(Xml.CAR_OWNERS);
        	String[] owners = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("car owners: "+names);
        	setNames(owners);
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

    static Logger log = Logger.getLogger(CarOwners.class.getName());

}

