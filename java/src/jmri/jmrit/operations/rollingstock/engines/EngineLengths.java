// EngineLengths.java

package jmri.jmrit.operations.rollingstock.engines;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

import org.jdom.Attribute;
import org.jdom.Element;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the lengths that engines can have.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class EngineLengths {

	private static final String LENGTHS = Bundle.getMessage("engineDefaultLengths");
	public static final String ENGINELENGTHS_CHANGED_PROPERTY = "EngineLengths"; // NOI18N
	public static final String ENGINELENGTHS_NAME_CHANGED_PROPERTY = "EngineLengthsName"; // NOI18N

	public EngineLengths() {
	}

	/** record the single instance **/
	private static EngineLengths _instance = null;

	public static synchronized EngineLengths instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("EngineLengths creating instance");
			// create and load
			_instance = new EngineLengths();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("EngineLengths returns instance " + _instance);
		return _instance;
	}

	public void dispose() {
		list.clear();
	}

	List<String> list = new ArrayList<String>();

	public String[] getNames() {
		if (list.size() == 0) {
			String[] lengths = LENGTHS.split("%%"); // NOI18N
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
			log.error("Locomotive lengths are not all numeric, list:");
			for (int i = 0; i < lengths.length; i++) {
				try {
					Integer.parseInt(lengths[i]);
					log.error("Loco length " + i + " = " + lengths[i]);
				} catch (NumberFormatException ee) {
					log.error("Loco length " + i + " = " + lengths[i] + " is not a valid number!");
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
		firePropertyChange(ENGINELENGTHS_CHANGED_PROPERTY, list.size() - 1, list.size());
	}

	public void deleteName(String length) {
		list.remove(length);
		firePropertyChange(ENGINELENGTHS_CHANGED_PROPERTY, list.size() + 1, list.size());
	}

	public boolean containsName(String length) {
		return list.contains(length);
	}

	public void replaceName(String oldName, String newName) {
		addName(newName);
		firePropertyChange(ENGINELENGTHS_NAME_CHANGED_PROPERTY, oldName, newName);
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
	
	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-engines.dtd.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public void store(Element root) {		
		String[]names = getNames();
		if (Control.backwardCompatible) {
			Element values = new Element(Xml.ENGINE_LENGTHS);
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
			if (log.isDebugEnabled()) log.debug("Engine lengths sees "+l.size()+" lengths");
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
		else if (root.getChild(Xml.ENGINE_LENGTHS)!= null){
        	String names = root.getChildText(Xml.ENGINE_LENGTHS);
        	String[] lengths = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("engine lengths: "+names);
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
		EngineManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EngineLengths.class
			.getName());

}
