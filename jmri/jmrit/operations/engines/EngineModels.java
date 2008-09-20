// CarTypes.java

package jmri.jmrit.operations.engines;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the various engine models a railroad can have.
 * Each model has a horsepower rating that is kept here.
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.5 $
 */
public class EngineModels implements java.beans.PropertyChangeListener {
	
	private static final String MODELS = "E8%%F7%%F8%%GP20%%GP30%%GP35%%RS18%%RS19%%RS27%%RS3%%RSD4%%SD26%%SD45%%SW1200%%SW1500%%SW8%%TRAINMASTER%%U28B";
	public static final String ENGINEMODELS = "EngineModels";
	private static final String LENGTH = "Length";
	
	protected Hashtable _engineHorsepowerHashTable = new Hashtable();
    
	public EngineModels() {
    }
    
	/** record the single instance **/
	private static EngineModels _instance = null;

	public static synchronized EngineModels instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineModels creating instance");
			// create and load
			_instance = new EngineModels();
			// load engines
			EngineManagerXml.instance();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("EngineModels returns instance "+_instance);
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
     		String[] types = MODELS.split("%%");
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
    	firePropertyChange (ENGINEMODELS, null, LENGTH);
    }
    
    public void deleteName(String type){
    	list.remove(type);
    	firePropertyChange (ENGINEMODELS, null, LENGTH);
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
    
    public void setModelHorsepower(String model, String horsepower){
    	_engineHorsepowerHashTable.put(model, horsepower);
    }
    
    public String getModelHorsepower(String model){
    	return (String)_engineHorsepowerHashTable.get(model);
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

