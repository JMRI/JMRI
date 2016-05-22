// CarLengths.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

import org.jdom.Attribute;
import org.jdom.Element;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the lengths that cars can have.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class CarLengths implements java.beans.PropertyChangeListener {

	private static final String LENGTHS = Bundle.getMessage("carLengths");
	public static final String CARLENGTHS_CHANGED_PROPERTY = "CarLengths";	// NOI18N
	public static final String CARLENGTHS_NAME_CHANGED_PROPERTY = "CarLengthsName";	// NOI18N

	private static final int MIN_NAME_LENGTH = 4;

	public CarLengths() {
	}

	/** record the single instance **/
	private static CarLengths _instance = null;

	public static synchronized CarLengths instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("CarLengths creating instance");
			// create and load
			_instance = new CarLengths();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("CarLengths returns instance " + _instance);
		return _instance;
	}

	public synchronized void dispose() {
		list.clear();
		// remove all listeners
		for (java.beans.PropertyChangeListener p : pcs.getPropertyChangeListeners())
			pcs.removePropertyChangeListener(p);
	}

	/**
	 * The PropertyChangeListener interface in this class is intended to keep track of user name changes to individual
	 * NamedBeans. It is not completely implemented yet. In particular, listeners are not added to newly registered
	 * objects.
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
	}

	List<String> list = new ArrayList<String>();

	public String[] getNames() {
		if (list.size() == 0) {
			String[] lengths = LENGTHS.split("%%");	// NOI18N
			for (int i = 0; i < lengths.length; i++)
				list.add(lengths[i]);
		}
		String[] lengths = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			lengths[i] = list.get(i);
		return lengths;
	}

	public void setNames(String[] lengths) {
		if (lengths.length == 0)
			return;
		try {
			jmri.util.StringUtil.numberSort(lengths);
		} catch (NumberFormatException e) {
			log.error("Car lengths are not all numeric, list:");
			for (int i = 0; i < lengths.length; i++) {
				try {
					Integer.parseInt(lengths[i]);
					log.error("Car length " + i + " = " + lengths[i]);
				} catch (NumberFormatException ee) {
					log.error("Car length " + i + " = " + lengths[i] + " is not a valid number!");
				}
			}
		}
		for (int i = 0; i < lengths.length; i++)
			if (!list.contains(lengths[i]))
				list.add(lengths[i]);
	}

	public void addName(String length) {
		// insert at start of list, sort later
		if (list.contains(length))
			return;
		list.add(0, length);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARLENGTHS_CHANGED_PROPERTY, list.size() - 1, list.size());
	}

	public void deleteName(String length) {
		list.remove(length);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARLENGTHS_CHANGED_PROPERTY, list.size() + 1, list.size());
	}

	public boolean containsName(String length) {
		return list.contains(length);
	}

	public void replaceName(String oldName, String newName) {
		addName(newName);
		firePropertyChange(CARLENGTHS_NAME_CHANGED_PROPERTY, oldName, newName);
		// need to keep old name so location manager can replace properly
		deleteName(oldName);
	}

	public JComboBox getComboBox() {
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

	private int maxNameLength = 0;

	public int getCurMaxNameLength() {
		if (maxNameLength == 0) {
			String[] lengths = getNames();
			int length = MIN_NAME_LENGTH;
			for (int i = 0; i < lengths.length; i++) {
				if (lengths[i].length() > length)
					length = lengths[i].length();
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
			Element values = new Element(Xml.CAR_LENGTHS);
			for (int i=0; i<names.length; i++){
				String lengthNames = names[i]+"%%"; // NOI18N
				values.addContent(lengthNames);
			}
			root.addContent(values);
		}
		// new format using elements
		Element lengths = new Element(Xml.LENGTHS);
		for (int i=0; i<names.length; i++){
			Element length = new Element(Xml.LENGTH);
			length.setAttribute(new Attribute(Xml.VALUE, names[i]));
			lengths.addContent(length);
		}
		root.addContent(lengths);
	}
	
	public void load(Element root) {
		// new format using elements starting version 3.3.1
		if (root.getChild(Xml.LENGTHS)!= null){
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.LENGTHS).getChildren(Xml.LENGTH);
			if (log.isDebugEnabled()) log.debug("CarLengths sees "+l.size()+" lengths");
			Attribute a;
			String[] lengths = new String[l.size()];
			for (int i=0; i<l.size(); i++) {
				Element length = l.get(i);
				if ((a = length.getAttribute(Xml.VALUE)) != null) {
					lengths[i] = a.getValue();
				}
			}
			setNames(lengths);
		}
		// old format
		else if (root.getChild(Xml.CAR_LENGTHS)!= null){
        	String names = root.getChildText(Xml.CAR_LENGTHS);
        	String[] lengths = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("car lengths: "+names);
        	setNames(lengths);
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

	static Logger log = LoggerFactory.getLogger(CarLengths.class
			.getName());

}
