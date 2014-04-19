// CarRoads.java

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
 * Represents the road names that cars can have.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class CarRoads {

	private static final String ROADS = Bundle.getMessage("carRoadNames");
	public static final String CARROADS_LENGTH_CHANGED_PROPERTY = "CarRoads Length"; // NOI18N
	public static final String CARROADS_NAME_CHANGED_PROPERTY = "CarRoads Name"; // NOI18N

	private static final int MIN_NAME_LENGTH = 4;

	public CarRoads() {
	}

	/** record the single instance **/
	private static CarRoads _instance = null;

	public static synchronized CarRoads instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("CarRoads creating instance");
			// create and load
			_instance = new CarRoads();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("CarRoads returns instance " + _instance);
		return _instance;
	}

	public synchronized void dispose() {
		list.clear();
	}

	List<String> list = new ArrayList<String>();

	public String[] getNames() {
		if (list.size() == 0) {
			String[] roads = ROADS.split(","); // NOI18N
			for (int i = 0; i < roads.length; i++)
				list.add(roads[i]);
		}
		String[] roads = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			roads[i] = list.get(i);
		return roads;
	}

	public void setNames(String[] roads) {
		if (roads.length == 0)
			return;
		jmri.util.StringUtil.sort(roads);
		for (int i = 0; i < roads.length; i++)
			if (!list.contains(roads[i]))
				list.add(roads[i]);
	}

	public void addName(String road) {
		if (road == null)
			return;
		// insert at start of list, sort later
		if (list.contains(road))
			return;
		list.add(0, road);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARROADS_LENGTH_CHANGED_PROPERTY, list.size() - 1, list.size());
	}

	public void deleteName(String road) {
		list.remove(road);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARROADS_LENGTH_CHANGED_PROPERTY, list.size() + 1, list.size());
	}

	public void replaceName(String oldName, String newName) {
		addName(newName);
		list.remove(oldName);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARROADS_NAME_CHANGED_PROPERTY, oldName, newName);
		if (newName == null)
			firePropertyChange(CARROADS_LENGTH_CHANGED_PROPERTY, list.size() + 1, list.size());
	}

	public boolean containsName(String road) {
		return list.contains(road);
	}

	public JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		String[] roads = getNames();
		for (int i = 0; i < roads.length; i++)
			box.addItem(roads[i]);
		return box;
	}

	public void updateComboBox(JComboBox box) {
		box.removeAllItems();
		String[] roads = getNames();
		for (int i = 0; i < roads.length; i++)
			box.addItem(roads[i]);
	}

	private int maxNameLength = 0;

	public int getCurMaxNameLength() {
		if (maxNameLength == 0) {
			String[] roads = getNames();
			int length = MIN_NAME_LENGTH;
			for (int i = 0; i < roads.length; i++) {
				if (roads[i].length() > length)
					length = roads[i].length();
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
			Element values = new Element(Xml.ROAD_NAMES);
			for (int i=0; i<names.length; i++){
				String roadNames = names[i]+"%%"; // NOI18N
				values.addContent(roadNames);
			}
			root.addContent(values);
		}
        // new format using elements
        Element roads = new Element(Xml.ROADS);
        for (int i=0; i<names.length; i++){
        	Element road = new Element(Xml.ROAD);
        	road.setAttribute(new Attribute(Xml.NAME, names[i]));
        	roads.addContent(road);
        }
        root.addContent(roads);
	}
	
	public void load(Element root) {
		// new format using elements starting version 3.3.1
		if (root.getChild(Xml.ROADS)!= null){
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.ROADS).getChildren(Xml.ROAD);
			if (log.isDebugEnabled()) log.debug("Car roads sees "+l.size()+" roads");
			Attribute a;
			String[] roads = new String[l.size()];
			for (int i=0; i<l.size(); i++) {
				Element road = l.get(i);
				if ((a = road.getAttribute(Xml.NAME)) != null) {
					roads[i] = a.getValue();
				}
			}
			setNames(roads);
		}
		// old format
		else if (root.getChild(Xml.ROAD_NAMES)!= null){
        	String names = root.getChildText(Xml.ROAD_NAMES);
        	String[] roads = names.split("%%"); // NOI18N
        	if (log.isDebugEnabled()) log.debug("road names: "+names);
        	setNames(roads);
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

	static Logger log = LoggerFactory
			.getLogger(CarRoads.class.getName());
}
