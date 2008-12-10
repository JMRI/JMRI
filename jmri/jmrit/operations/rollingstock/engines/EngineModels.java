// EngineModels.java

package jmri.jmrit.operations.rollingstock.engines;

import java.util.Enumeration;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the various engine models a railroad can have.
 * Each model has a horsepower rating and length that is kept here.
 * The program provides some default models for the user.  These values
 * can be overridden by the user.
 * 
 * Model Horsepower Length 	Type
 * E8		2250	70		Diesel
 * FT		1350	50		Diesel		
 * F3		1500	50		Diesel
 * F7		1500	50		Diesel
 * F9		1750	50		Diesel
 * GP20		2000	56		Diesel
 * GP30		2250	56		Diesel
 * GP35		2500	56		Diesel
 * GP38		2000	59		Diesel
 * GP40		3000	59		Diesel
 * RS1		1000	51		Diesel
 * RS2		1500	52		Diesel
 * RS3		1600	51		Diesel
 * RS11		1800	53		Diesel
 * RS18		1800	52		Diesel
 * RS27		2400	57		Diesel
 * RSD4		1600	52		Diesel
 * SD26		2650	61		Diesel
 * SD45		3600	66		Diesel
 * SW1200	1200	45		Diesel
 * SW1500	1500	45		Diesel
 * SW8		800		44		Diesel
 * TRAINMASTER	2400	66	Diesel 
 * U28B		2800	60		Diesel
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version	$Revision: 1.5 $
 */
public class EngineModels implements java.beans.PropertyChangeListener {
	
	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");

	private static final String MODELS = rb.getString("engineDefaultModels");
	// Horsepower, length, and type have a one to one correspondence with the above MODELS
	private static final String HORSEPOWER = rb.getString("engineModelHorsepowers");
	private static final String ENGINELENGTHS = rb.getString("engineModelLengths");
	private static final String ENGINETYPES = rb.getString("engineModelTypes");
	
	public static final String ENGINEMODELS_CHANGED_PROPERTY = "EngineModels";
	private static final String LENGTH = "Length";
	
	protected Hashtable _engineHorsepowerHashTable = new Hashtable();
	protected Hashtable _engineLengthHashTable = new Hashtable();
	protected Hashtable _engineTypeHashTable = new Hashtable();
    
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
    	firePropertyChange (ENGINEMODELS_CHANGED_PROPERTY, null, LENGTH);
    }
    
    public void deleteName(String model){
    	list.remove(model);
    	firePropertyChange (ENGINEMODELS_CHANGED_PROPERTY, null, LENGTH);
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
    
    public void setModelType(String model, String type){
    	_engineTypeHashTable.put(model, type);
    }
    
    public String getModelType(String model){
    	return (String)_engineTypeHashTable.get(model);
    }
    
    private void loadDefaults(){
		String[] models = MODELS.split("%%");
 		String[] hps = HORSEPOWER.split("%%");
 		String[] lengths = ENGINELENGTHS.split("%%"); 
 		String[] types = ENGINETYPES.split("%%"); 
 		if (models.length != hps.length || models.length != lengths.length || models.length != types.length){
 			log.error("Defaults do not have the right number of items, " +
 					"models="+models.length+" hps="+hps.length+" lengths="+lengths.length+" types="+types.length);
 			return;
 		}
 			
 		for (int i=0; i<models.length; i++){
 			setModelHorsepower(models[i], hps[i]);
 			setModelLength(models[i], lengths[i]);
 			setModelType(models[i], types[i]);
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

