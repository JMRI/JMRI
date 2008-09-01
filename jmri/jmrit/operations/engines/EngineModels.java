// CarTypes.java

package jmri.jmrit.operations.engines;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;


/**
 * Represents the types of cars a railroad can have.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class EngineModels implements java.beans.PropertyChangeListener {
	
	private static final String TYPES = "";
	public static final String ENGINETYPES = "EngineTypes";
	private static final String LENGTH = "Length";
    
	public EngineModels() {
    }
    
	/** record the single instance **/
	private static EngineModels _instance = null;

	public static synchronized EngineModels instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineTypes creating instance");
			// create and load
			_instance = new EngineModels();
			// load cars
			EngineManagerXml.instance();
		}
		if (log.isDebugEnabled()) log.debug("EngineTypes returns instance "+_instance);
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
     		String[] types = TYPES.split("%%");
     		for (int i=0; i<types.length; i++)
     			list.add(types[i]);
    	}
     	String[] types = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		types[i] = (String)list.get(i);
   		return types;
    }
    
    public void setNames(String[] types){
    	if (types.length == 0) return;
    	jmri.util.StringUtil.sort(types);
 		for (int i=0; i<types.length; i++)
 			list.add(types[i]);
    }
    
    public void addName(String type){
    	// insert at start of list, sort later
    	if (list.contains(type))
    		return;
    	list.add(0,type);
    	firePropertyChange (ENGINETYPES, null, LENGTH);
    }
    
    public void deleteName(String type){
    	list.remove(type);
    	firePropertyChange (ENGINETYPES, null, LENGTH);
     }
    
    public boolean containsName(String type){
    	return list.contains(type);
     }
    
    public JComboBox getComboBox (){
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EngineModels.class.getName());

}

