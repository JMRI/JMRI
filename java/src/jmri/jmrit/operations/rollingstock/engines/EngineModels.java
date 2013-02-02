// EngineModels.java

package jmri.jmrit.operations.rollingstock.engines;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JComboBox;

import org.jdom.Attribute;
import org.jdom.Element;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the various engine models a railroad can have. Each model has a type, horsepower rating and length that is
 * kept here. The program provides some default models for the user. These values can be overridden by the user.
 * 
 * Model Horsepower Length Type E8 2250 70 Diesel FT 1350 50 Diesel F3 1500 50 Diesel F7 1500 50 Diesel F9 1750 50
 * Diesel GP20 2000 56 Diesel GP30 2250 56 Diesel GP35 2500 56 Diesel GP38 2000 59 Diesel GP40 3000 59 Diesel RS1 1000
 * 51 Diesel RS2 1500 52 Diesel RS3 1600 51 Diesel RS11 1800 53 Diesel RS18 1800 52 Diesel RS27 2400 57 Diesel RSD4 1600
 * 52 Diesel SD26 2650 61 Diesel SD45 3600 66 Diesel SW1200 1200 45 Diesel SW1500 1500 45 Diesel SW8 800 44 Diesel
 * TRAINMASTER 2400 66 Diesel U28B 2800 60 Diesel
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class EngineModels {

	private static final String MODELS = Bundle.getMessage("engineDefaultModels");
	// Horsepower, length, and type have a one to one correspondence with the above MODELS
	private static final String HORSEPOWER = Bundle.getMessage("engineModelHorsepowers");
	private static final String ENGINELENGTHS = Bundle.getMessage("engineModelLengths");
	private static final String ENGINETYPES = Bundle.getMessage("engineModelTypes");
	private static final String ENGINEWEIGHTS = Bundle.getMessage("engineModelWeights");

	public static final String ENGINEMODELS_CHANGED_PROPERTY = "EngineModels"; // NOI18N
	public static final String ENGINEMODELS_NAME_CHANGED_PROPERTY = "EngineModelsName"; // NOI18N

	protected List<String> _list = new ArrayList<String>();
	protected Hashtable<String, String> _engineHorsepowerHashTable = new Hashtable<String, String>();
	protected Hashtable<String, String> _engineLengthHashTable = new Hashtable<String, String>();
	protected Hashtable<String, String> _engineTypeHashTable = new Hashtable<String, String>();
	protected Hashtable<String, String> _engineWeightHashTable = new Hashtable<String, String>();

	public EngineModels() {
	}

	/** record the single instance **/
	private static EngineModels _instance = null;

	public static synchronized EngineModels instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("EngineModels creating instance");
			// create and load
			_instance = new EngineModels();
			_instance.loadDefaults();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("EngineModels returns instance " + _instance);
		return _instance;
	}

	public void dispose() {
		_list.clear();
		_engineHorsepowerHashTable.clear();
		_engineLengthHashTable.clear();
		_engineTypeHashTable.clear();
		_engineWeightHashTable.clear();
		loadDefaults();
	}

	public String[] getNames() {
		if (_list.size() == 0) {
			String[] types = MODELS.split("%%"); // NOI18N
			for (int i = 0; i < types.length; i++)
				_list.add(types[i]);
		}
		String[] models = new String[_list.size()];
		for (int i = 0; i < _list.size(); i++)
			models[i] = _list.get(i);
		return models;
	}

	public void setNames(String[] models) {
		if (models.length == 0)
			return;
		jmri.util.StringUtil.sort(models);
		for (int i = 0; i < models.length; i++)
			if (!_list.contains(models[i]))
				_list.add(models[i]);
	}

	public void addName(String model) {
		// insert at start of list, sort later
		if (_list.contains(model))
			return;
		_list.add(0, model);
		firePropertyChange(ENGINEMODELS_CHANGED_PROPERTY, _list.size() - 1, _list.size());
	}

	public void deleteName(String model) {
		if (!_list.contains(model))
			return;
		_list.remove(model);
		firePropertyChange(ENGINEMODELS_CHANGED_PROPERTY, _list.size() + 1, _list.size());
	}

	public boolean containsName(String model) {
		return _list.contains(model);
	}

	public void replaceName(String oldName, String newName) {
		addName(newName);
		firePropertyChange(ENGINEMODELS_NAME_CHANGED_PROPERTY, oldName, newName);
		deleteName(oldName);
	}

	public JComboBox getComboBox() {
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

	public void setModelHorsepower(String model, String horsepower) {
		_engineHorsepowerHashTable.put(model, horsepower);
	}

	public String getModelHorsepower(String model) {
		return _engineHorsepowerHashTable.get(model);
	}

	public void setModelLength(String model, String horsepower) {
		_engineLengthHashTable.put(model, horsepower);
	}

	public String getModelLength(String model) {
		return _engineLengthHashTable.get(model);
	}

	public void setModelType(String model, String type) {
		_engineTypeHashTable.put(model, type);
	}

	public String getModelType(String model) {
		return _engineTypeHashTable.get(model);
	}

	public void setModelWeight(String model, String type) {
		_engineWeightHashTable.put(model, type);
	}

	/**
	 * 
	 * @param model
	 *            The engine model (example GP20)
	 * @return This model's weight in tons
	 */
	public String getModelWeight(String model) {
		return _engineWeightHashTable.get(model);
	}

	private void loadDefaults() {
		String[] models = MODELS.split("%%"); // NOI18N
		String[] hps = HORSEPOWER.split("%%"); // NOI18N
		String[] lengths = ENGINELENGTHS.split("%%"); // NOI18N
		String[] types = ENGINETYPES.split("%%"); // NOI18N
		String[] weights = ENGINEWEIGHTS.split("%%"); // NOI18N
		if (models.length != hps.length || models.length != lengths.length
				|| models.length != types.length || models.length != weights.length) {
			log.error("Defaults do not have the right number of items, " + "models="
					+ models.length + " hps=" + hps.length + " lengths=" + lengths.length // NOI18N
					+ " types=" + types.length); // NOI18N
			return;
		}

		for (int i = 0; i < models.length; i++) {
			setModelHorsepower(models[i], hps[i]);
			setModelLength(models[i], lengths[i]);
			setModelType(models[i], types[i]);
			setModelWeight(models[i], weights[i]);
		}
	}
	
	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-engines.dtd.
	 * 
	 */
	public void store(Element root) {
		String[]names = getNames();
		if (Control.backwardCompatible) {
			Element values = new Element(Xml.ENGINE_MODELS);
			for (int i=0; i<names.length; i++){
				String typeNames = names[i]+"%%"; // NOI18N
				values.addContent(typeNames);
			}
			root.addContent(values);
		}
		// new format using elements
		Element types = new Element(Xml.MODELS);
		for (int i=0; i<names.length; i++){
			Element type = new Element(Xml.MODEL);
			type.setAttribute(new Attribute(Xml.NAME, names[i]));
			types.addContent(type);
		}
		root.addContent(types);
	}
	
	public void load(Element root) {
		// new format using elements starting version 3.3.1
		if (root.getChild(Xml.MODELS)!= null){
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.MODELS).getChildren(Xml.MODEL);
			if (log.isDebugEnabled()) log.debug("Engine models sees "+l.size()+" models");
			Attribute a;
			String[] types = new String[l.size()];
			for (int i=0; i<l.size(); i++) {
				Element type = l.get(i);
				if ((a = type.getAttribute(Xml.NAME)) != null) {
					types[i] = a.getValue();
				}
			}
			setNames(types);
		}
		// old format
		else if (root.getChild(Xml.ENGINE_MODELS)!= null){
			String names = root.getChildText(Xml.ENGINE_MODELS);
			String[] types = names.split("%%"); // NOI18N
			if (log.isDebugEnabled()) log.debug("engine models: "+names);
			setNames(types);
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
		EngineManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = Logger.getLogger(EngineModels.class
			.getName());

}
