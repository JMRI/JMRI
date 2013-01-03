// EngineTypes.java

package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the types of engines a railroad can have.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class EngineTypes {

	private static final String TYPES = Bundle.getMessage("engineDefaultTypes");

	// for property change
	public static final String ENGINETYPES_LENGTH_CHANGED_PROPERTY = "EngineTypesLength"; // NOI18N
	public static final String ENGINETYPES_NAME_CHANGED_PROPERTY = "EngineTypesName"; // NOI18N

	public EngineTypes() {
	}

	/** record the single instance **/
	private static EngineTypes _instance = null;

	public static synchronized EngineTypes instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("EngineTypes creating instance");
			// create and load
			_instance = new EngineTypes();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("EngineTypes returns instance " + _instance);
		return _instance;
	}

	public void dispose() {
		list.clear();
	}

	List<String> list = new ArrayList<String>();

	public String[] getNames() {
		if (list.size() == 0) {
			String[] types = TYPES.split("%%"); // NOI18N
			for (int i = 0; i < types.length; i++)
				list.add(types[i]);
		}
		String[] types = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			types[i] = list.get(i);
		return types;
	}

	public void setNames(String[] types) {
		if (types.length == 0)
			return;
		jmri.util.StringUtil.sort(types);
		for (int i = 0; i < types.length; i++)
			if (!list.contains(types[i]))
				list.add(types[i]);
	}

	public void addName(String type) {
		if (type == null)
			return;
		// insert at start of list, sort later
		if (list.contains(type))
			return;
		list.add(0, type);
		firePropertyChange(ENGINETYPES_LENGTH_CHANGED_PROPERTY, list.size() - 1, list.size());
	}

	public void deleteName(String type) {
		if (!list.contains(type))
			return;
		list.remove(type);
		firePropertyChange(ENGINETYPES_LENGTH_CHANGED_PROPERTY, list.size() + 1, list.size());
	}

	public boolean containsName(String type) {
		return list.contains(type);
	}

	public void replaceName(String oldName, String newName) {
		addName(newName);
		firePropertyChange(ENGINETYPES_NAME_CHANGED_PROPERTY, oldName, newName);
		// need to keep old name so location manager can replace properly
		deleteName(oldName);
	}

	public JComboBox getComboBox() {
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

	protected void firePropertyChange(String p, Object old, Object n) {
		// Set dirty
		EngineManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineTypes.class
			.getName());

}
