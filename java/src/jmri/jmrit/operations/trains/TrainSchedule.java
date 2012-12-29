package jmri.jmrit.operations.trains;

import java.util.ArrayList;
import java.util.List;

import jmri.jmrit.operations.setup.Control;

import org.jdom.Element;

/**
 * Represents a schedule for trains
 * 
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class TrainSchedule {

	public static final String NAME_CHANGED_PROPERTY = "trainScheduleName";
	public static final String SCHEDULE_CHANGED_PROPERTY = "trainScheduleChanged";

	protected String _id = "";
	protected String _name = "";
	protected String _comment = "";
	protected List<String> _trainIds = new ArrayList<String>();

	public TrainSchedule(String id, String name) {
		log.debug("New train schedule " + name + " " + id);
		_name = name;
		_id = id;
	}

	public String getId() {
		return _id;
	}

	public void setName(String name) {
		String old = _name;
		_name = name;
		if (!old.equals(name)) {
			firePropertyChange(NAME_CHANGED_PROPERTY, old, name);
		}
	}

	// for combo boxes
	public String toString() {
		return _name;
	}

	public String getName() {
		return _name;
	}

	public void setComment(String comment) {
		String old = _comment;
		_comment = comment;
		if (!old.equals(comment))
			firePropertyChange("AddTrainScheduleComment", old, comment);
	}

	public String getComment() {
		return _comment;
	}

	public void addTrainId(String id) {
		if (!_trainIds.contains(id)) {
			_trainIds.add(id);
			firePropertyChange(SCHEDULE_CHANGED_PROPERTY, null, id);
		}
	}

	public void removeTrainId(String id) {
		_trainIds.remove(id);
		firePropertyChange(SCHEDULE_CHANGED_PROPERTY, id, null);
	}

	public boolean containsTrainId(String id) {
		return _trainIds.contains(id);
	}

	/**
	 * Construct this Entry from XML. This member has to remain synchronized with the detailed DTD in
	 * operations-trains.xml
	 * 
	 * @param e
	 *            Consist XML element
	 */
	public TrainSchedule(Element e) {
		// if (log.isDebugEnabled()) log.debug("ctor from element "+e);
		org.jdom.Attribute a;
		if ((a = e.getAttribute("id")) != null)
			_id = a.getValue();
		else
			log.warn("no id attribute in schedule element when reading operations");
		if ((a = e.getAttribute("name")) != null)
			_name = a.getValue();
		if ((a = e.getAttribute("comment")) != null)
			_comment = a.getValue();
		if ((a = e.getAttribute("trainIds")) != null) {
			String ids = a.getValue();
			String[] trainIds = ids.split(",");
			for (int i = 0; i < trainIds.length; i++) {
				_trainIds.add(trainIds[i]);
			}
			if (log.isDebugEnabled())
				log.debug("Train schedule " + getName() + " trainIds: " + ids);
		}
	}

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-config.xml.
	 * 
	 * @return Contents in a JDOM Element
	 */
	public Element store() {
		Element e = new org.jdom.Element("schedule");
		e.setAttribute("id", getId());
		e.setAttribute("name", getName());
		if (!getComment().equals(""))
			e.setAttribute("comment", getComment());
		// store train ids
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < _trainIds.size(); i++) {
			buf.append(_trainIds.get(i) + ",");
		}
		e.setAttribute("trainIds", buf.toString());
		return e;
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("schedule (" + getName() + ") sees property change: " + e.getPropertyName()
					+ " from (" + e.getSource() + ") old: " + e.getOldValue() + " new: "
					+ e.getNewValue());
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	protected void firePropertyChange(String p, Object old, Object n) {
		TrainManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainSchedule.class
			.getName());

}
