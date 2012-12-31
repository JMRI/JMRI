// CarColors.java

package jmri.jmrit.operations.rollingstock.cars;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;

import jmri.jmrit.operations.setup.Control;

/**
 * Represents the colors that cars can have.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision$
 */
public class CarColors {

	private static final String COLORS = Bundle.getString("carColors");
	public static final String CARCOLORS_CHANGED_PROPERTY = "CarColors";	// NOI18N
	public static final String CARCOLORS_NAME_CHANGED_PROPERTY = "CarColorsName";	// NOI18N

	private static final int MIN_NAME_LENGTH = 4;

	public CarColors() {
	}

	/** record the single instance **/
	private static CarColors _instance = null;

	public static synchronized CarColors instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("CarColors creating instance");
			// create and load
			_instance = new CarColors();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("CarColors returns instance " + _instance);
		return _instance;
	}

	public synchronized void dispose() {
		list.clear();
		// remove all listeners
		for (java.beans.PropertyChangeListener p : pcs.getPropertyChangeListeners())
			pcs.removePropertyChangeListener(p);
	}

	List<String> list = new ArrayList<String>();

	public String[] getNames() {
		if (list.size() == 0) {
			String[] colors = COLORS.split("%%");	// NOI18N
			for (int i = 0; i < colors.length; i++)
				list.add(colors[i]);
		}
		String[] colors = new String[list.size()];
		for (int i = 0; i < list.size(); i++)
			colors[i] = list.get(i);
		return colors;
	}

	public void setNames(String[] colors) {
		if (colors.length == 0)
			return;
		jmri.util.StringUtil.sort(colors);
		for (int i = 0; i < colors.length; i++)
			if (!list.contains(colors[i]))
				list.add(colors[i]);
	}

	public void addName(String color) {
		// insert at start of list, sort later
		if (list.contains(color))
			return;
		list.add(0, color);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARCOLORS_CHANGED_PROPERTY, list.size() - 1, list.size());
	}

	public void deleteName(String color) {
		list.remove(color);
		maxNameLength = 0; // reset maximum name length
		firePropertyChange(CARCOLORS_CHANGED_PROPERTY, list.size() + 1, list.size());
	}

	public boolean containsName(String color) {
		return list.contains(color);
	}

	public void replaceName(String oldName, String newName) {
		addName(newName);
		firePropertyChange(CARCOLORS_NAME_CHANGED_PROPERTY, oldName, newName);
		// need to keep old name so location manager can replace properly
		deleteName(oldName);
	}

	public JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		String[] colors = getNames();
		for (int i = 0; i < colors.length; i++)
			box.addItem(colors[i]);
		return box;
	}

	public void updateComboBox(JComboBox box) {
		box.removeAllItems();
		String[] colors = getNames();
		for (int i = 0; i < colors.length; i++)
			box.addItem(colors[i]);
	}

	private int maxNameLength = 0;

	public int getCurMaxNameLength() {
		if (maxNameLength == 0) {
			String[] colors = getNames();
			int length = MIN_NAME_LENGTH;
			for (int i = 0; i < colors.length; i++) {
				if (colors[i].length() > length)
					length = colors[i].length();
			}
			maxNameLength = length;
			return length;
		} else {
			return maxNameLength;
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

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarColors.class
			.getName());

}
