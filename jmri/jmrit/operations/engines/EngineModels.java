// EngineModels.java

package jmri.jmrit.operations.engines;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the various engine models a railroad can have.
 * Each model has a horsepower rating and length that is kept here.
 * The program provides some default models for the user.  These values
 * can be overridden by the user.
 * 
 * Model Horsepower Length
 * E8		2250	70
 * FT		1350	50		
 * F3		1500	50
 * F7		1500	50
 * F9		1750	50
 * GP20		2000	56
 * GP30		2250	56
 * GP35		2500	56
 * GP38		2000	59
 * GP40		3000	59
 * RS1		1000	51
 * RS2		1500	52
 * RS3		1600	51
 * RS11		1800	53
 * RS18		1800	52
 * RS27		2400	57
 * RSD4		1600	52
 * SD26		2650	61
 * SD45		3600	66
 * SW1200	1200	45
 * SW1500	1500	45
 * SW8		800		44
 * TRAINMASTER	2400	66 
 * U28B		2800	60
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.6 $
 */
public class EngineModels implements java.beans.PropertyChangeListener {
	
	private static final String MODELS = "E8%%FT%%F3%%F7%%F9%%GP20%%GP30%%GP35%%GP38%%GP40%%" +
			"RS1%%RS2%%RS3%%RS11%%RS18%%RS27%%RSD4%%SD26%%SD45%%SW1200%%" +
			"SW1500%%SW8%%TRAINMASTER%%U28B";
	// Horsepower and length have a one to one correspondence with the above MODELS
	private static final String HORSEPOWER = "2250%%1350%%1500%%1500%%1750%%2000%%2250%%2500%%2000%%3000%%" +
			"1000%%1500%%1600%%1800%%1800%%2400%%1600%%2650%%3600%%1200%%" +
			"1500%%800%%2400%%2800";
	private static final String ENGINELENGTHS = "70%%50%%50%%50%%50%%56%%56%%56%%59%%59%%" +
			"51%%52%%51%%53%%52%%57%%52%%61%%66%%45%%" +
			"45%%44%%66%%60";
	
	public static final String ENGINEMODELS = "EngineModels";
	private static final String LENGTH = "Length";
	
	protected Hashtable _engineHorsepowerHashTable = new Hashtable();
	protected Hashtable _engineLengthHashTable = new Hashtable();
    
	public EngineModels() {
    }
    
	/** record the single instance **/
	private static EngineModels _instance = null;

	public static synchronized EngineModels instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("EngineModels creating instance");
			// create and load
			_instance = new EngineModels();
			_instance.loadDefaults();
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
     	String[] models = new String[list.size()];
     	for (int i=0; i<list.size(); i++)
     		models[i] = (String)list.get(i);
   		return models;
    }
    
    public void setNames(String[] models){
    	if (models.length == 0) return;
    	jmri.util.StringUtil.sort(models);
 		for (int i=0; i<models.length; i++)
 			list.add(models[i]);
    }
    
    public void addName(String model){
    	// insert at start of list, sort later
    	if (list.contains(model))
    		return;
    	list.add(0,model);
    	firePropertyChange (ENGINEMODELS, null, LENGTH);
    }
    
    public void deleteName(String model){
    	list.remove(model);
    	firePropertyChange (ENGINEMODELS, null, LENGTH);
     }
    
    public boolean containsName(String model){
    	return list.contains(model);
     }
    
    public JComboBox getComboBox (){
    	JComboBox box = new JComboBox();
		String[] models = getNames();
		for (int i = 0; i < models.length; i++)
			box.addItem(models[i]);
    	return box;
    }
    
    public void updateComboBox(JComboBox box) {
    	box.removeAllItems();
		String[] models = getNames();
		for (int i = 0; i < models.length; i++)
			box.addItem(models[i]);
    }
    
    public void setModelHorsepower(String model, String horsepower){
    	_engineHorsepowerHashTable.put(model, horsepower);
    }
    
    public String getModelHorsepower(String model){
    	return (String)_engineHorsepowerHashTable.get(model);
    }
    
    public void setModelLength(String model, String horsepower){
    	_engineLengthHashTable.put(model, horsepower);
    }
    
    public String getModelLength(String model){
    	return (String)_engineLengthHashTable.get(model);
    }
    
    private void loadDefaults(){
		String[] models = MODELS.split("%%");
 		String[] hps = HORSEPOWER.split("%%");
 		String[] lengths = ENGINELENGTHS.split("%%"); 
 		for (int i=0; i<models.length; i++){
 			setModelHorsepower(models[i], hps[i]);
 			setModelLength(models[i], lengths[i]);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EngineModels.class.getName());

}

