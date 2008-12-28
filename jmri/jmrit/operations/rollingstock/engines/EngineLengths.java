// EngineLengths.java

package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;


/**
 * Represents the lengths that engines can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.6 $
 */
public class EngineLengths implements java.beans.PropertyChangeListener {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	private static final String LENGTHS = rb.getString("engineDefaultLengths");
	public static final String ENGINELENGTHS_CHANGED_PROPERTY = "EngineLengths";
	private static final String LENGTH = "Length";
	
    public EngineLengths() {
    }
    
	/** record the single instance **/
	private static EngineLengths _instance = null;

	public static synchronized EngineLengths instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineLengths creating instance");
			// create and load
			_instance = new EngineLengths();
		}
		if (log.isDebugEnabled()) log.debug("EngineLengths returns instance "+_instance);
		return _instance;
	}

    public void dispose() {
    	list.clear();
    }
    
    /**
     * The PropertyChangeListener interface in this class is
     * intended to keep track of user name changes to individual NamedBeans.
     * It is not completely implemented yet. In particular, listeners
     * are not added to newly registered objects.
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    }

    List list = new ArrayList();
    
    public String[] getNames(){
     	if (list.size() == 0){
     		String[] lengths = LENGTHS.split("%%");
     		for (int i=0; i<lengths.length; i++)
     			list.add(lengths[i]);
    	}
     	String[] lengths = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		lengths[i] = (String)list.get(i);
   		return lengths;
    }
    
    public void setNames(String[] lengths){
    	if (lengths.length == 0) return;
    	jmri.util.StringUtil.sort(lengths);
 		for (int i=0; i<lengths.length; i++)
 			if (!list.contains(lengths[i]))
 				list.add(lengths[i]);
    }
    
    public void addName(String length){
    	// insert at start of list, sort later
    	if (list.contains(length))
    		return;
    	list.add(0,length);
    	firePropertyChange (ENGINELENGTHS_CHANGED_PROPERTY, null, LENGTH);
    }
    
    public void deleteName(String length){
    	list.remove(length);
    	firePropertyChange (ENGINELENGTHS_CHANGED_PROPERTY, null, LENGTH);
    }
     
    public boolean containsName(String length){
    	return list.contains(length);
     }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
		String[] lengths = getNames();
		for (int i = 0; i < lengths.length; i++)
			box.addItem(lengths[i]);
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		String[] lengths = getNames();
		for (int i = 0; i < lengths.length; i++)
			box.addItem(lengths[i]);
    }
        
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EngineLengths.class.getName());

}

