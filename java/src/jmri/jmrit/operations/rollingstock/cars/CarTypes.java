// CarTypes.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

import org.jdom.Attribute;
import org.jdom.Element;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Represents the types of cars a railroad can have.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class CarTypes {

	private static final String TYPES = Bundle.getMessage("carTypeNames");
	private static final String CONVERTTYPES = Bundle.getMessage("carTypeConvert"); // Used to convert from ARR to
																					// Descriptive
	private static final String ARRTYPES = Bundle.getMessage("carTypeARR");
	// for property change
	public static final String CARTYPES_LENGTH_CHANGED_PROPERTY = "CarTypes Length"; // NOI18N
	public static final String CARTYPES_NAME_CHANGED_PROPERTY = "CarTypes Name"; // NOI18N

	private static final int MIN_NAME_LENGTH = 4;

	public CarTypes() {
	}

	/** record the single instance **/
	private static CarTypes _instance = null;

	public static synchronized CarTypes instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("CarTypes creating instance");
			// create and load
			_instance = new CarTypes();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("CarTypes returns instance " + _instance);
		return _instance;
	}

	public synchronized void dispose() {
		list.clear();
	}

	protected List<String> list = new ArrayList<String>();

	public String[] getNames() {
		if (list.size() == 0) {
			String[] types = TYPES.split("%%"); // NOI18N
			if (Setup.getCarTypes().equals(Setup.AAR))
				types = ARRTYPES.split("%%"); // NOI18N
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
		for (int i = 0; i < types.length; i++) {
			if (!list.contains(types[i]) && !types[i].equals("Engine")) {  // NOI18N old code used Engine as car type remove
				list.add(types[i]);
			}
		}
	}

	/**
	 * Changes the car types from descriptive to AAR, or the other way. Only removes the default car type names from the
	 * list
	 */
	public void changeDefaultNames(String type) {
		if (type.equals(Setup.DESCRIPTIVE)) {
			// first replace the types
			String[] convert = CONVERTTYPES.split("%%"); // NOI18N
			String[] types = TYPES.split("%%"); // NOI18N
			for (int i = 0; i < convert.length; i++) {
				replaceName(convert[i], types[i]);
			}
			// remove AAR types
			String[] aarTypes = ARRTYPES.split("%%"); // NOI18N
			for (int i = 0; i < aarTypes.length; i++)
				list.remove(aarTypes[i]);
			// add descriptive types
			for (int i = 0; i < types.length; i++) {
				if (!list.contains(types[i]))
					list.add(types[i]);
			}
		} else {
			// first replace the types
			String[] convert = CONVERTTYPES.split("%%"); // NOI18N
			String[] types = TYPES.split("%%"); // NOI18N
			for (int i = 0; i < convert.length; i++) {
				replaceName(types[i], convert[i]);
			}
			// remove descriptive types
			for (int i = 0; i < types.length; i++)
				list.remove(types[i]);
			// add AAR types
			types = ARRTYPES.split("%%"); // NOI18N
			for (int i = 0; i < types.length; i++) {
				if (!list.contains(types[i]))
					list.add(types[i]);
			}
		}
	}

	public void addName(String type) {
		if (type == null)
			return;
		// insert at start of list, sort later
		if (list.contains(type))
			return;
		list.add(0, type);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARTYPES_LENGTH_CHANGED_PROPERTY, list.size() - 1, list.size());
	}

	public void deleteName(String type) {
		list.remove(type);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARTYPES_LENGTH_CHANGED_PROPERTY, list.size() + 1, list.size());
	}

	public boolean containsName(String type) {
		return list.contains(type);
	}

	public void replaceName(String oldName, String newName) {
		addName(newName);
		firePropertyChange(CARTYPES_NAME_CHANGED_PROPERTY, oldName, newName);
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

	private int maxNameLength = 0;

	/**
	 * Get the maximum character length of a car type when printing on a manifest or switch list. Car subtypes or
	 * characters after the "-" are ignored.
	 * 
	 * @return the maximum character length of a car type
	 */
	public int getCurMaxNameLength() {
		if (maxNameLength == 0) {
			String[] types = getNames();
			int length = MIN_NAME_LENGTH;
			for (int i = 0; i < types.length; i++) {
				String type[] = types[i].split("-");
				if (type[0].length() > length)
					length = type[0].length();
			}
			maxNameLength = length;
			return length;
		} else {
			return maxNameLength;
		}
	}

	private int maxNameLengthSubType = 0;

	/**
	 * Get the maximum character length of a car type including the sub type characters.
	 * 
	 * @return the maximum character length of a car type
	 */
	public int getMaxNameSubTypeLength() {
		if (maxNameLengthSubType == 0) {
			String[] types = getNames();
			int length = MIN_NAME_LENGTH;
			for (int i = 0; i < types.length; i++) {
				if (types[i].length() > length)
					length = types[i].length();
			}
			maxNameLengthSubType = length;
			return length;
		} else {
			return maxNameLengthSubType;
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
			Element values = new Element(Xml.CAR_TYPES);
			for (int i=0; i<names.length; i++){
				String typeNames = names[i]+"%%"; // NOI18N
				values.addContent(typeNames);
			}
			root.addContent(values);
		}
		// new format using elements
		Element types = new Element(Xml.TYPES);
		for (int i=0; i<names.length; i++){
			Element type = new Element(Xml.TYPE);
			type.setAttribute(new Attribute(Xml.NAME, names[i]));
			types.addContent(type);
		}
		root.addContent(types);
	}
	
	public void load(Element root) {
		// new format using elements starting version 3.3.1
		if (root.getChild(Xml.TYPES)!= null){
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.TYPES).getChildren(Xml.TYPE);
			if (log.isDebugEnabled()) log.debug("Car types sees "+l.size()+" types");
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
		else if (root.getChild(Xml.CAR_TYPES)!= null){
			String names = root.getChildText(Xml.CAR_TYPES);
			String[] types = names.split("%%"); // NOI18N
			if (log.isDebugEnabled()) log.debug("car types: "+names);
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
		CarManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(CarTypes.class.getName());

}
