package jmri.jmrit.operations.locations.divisions;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.beans.Identifiable;
import jmri.beans.PropertyChangeSupport;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.LocationManagerXml;


/**
 * Represents a railroad division
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class Division extends PropertyChangeSupport implements Identifiable {

    public static final String NONE = "";

    protected String _id = NONE;
    protected String _name = NONE;
    protected String _comment = NONE;

    public static final String NAME_CHANGED_PROPERTY = "divisionName"; // NOI18N

    public Division(String id, String name) {
        log.debug("New division ({}) id: {}", name, id);
        _name = name;
        _id = id;
    }

    @Override
    public String getId() {
        return _id;
    }

    /**
     * Sets the division's name.
     * 
     * @param name The string name for this division.
     *
     */
    public void setName(String name) {
        String old = _name;
        _name = name;
        if (!old.equals(name)) {
            setDirtyAndFirePropertyChange(NAME_CHANGED_PROPERTY, old, name);
        }
    }

    // for combo boxes
    @Override
    public String toString() {
        return _name;
    }

    public String getName() {
        return _name;
    }

    public void setComment(String comment) {
        String old = _comment;
        _comment = comment;
        if (!old.equals(comment)) {
            setDirtyAndFirePropertyChange("divisionComment", old, comment); // NOI18N
        }
    }

    public String getComment() {
        return _comment;
    }

    /**
     * Construct this Entry from XML. This member has to remain synchronized with
     * the detailed DTD in operations-locations.dtd
     *
     * @param e Consist XML element
     */
    @SuppressWarnings("deprecation") // until there's a replacement for convertFromXmlComment()
    public Division(Element e) {
        Attribute a;
        if ((a = e.getAttribute(Xml.ID)) != null) {
            _id = a.getValue();
        } else {
            log.warn("no id attribute in location element when reading operations");
        }
        if ((a = e.getAttribute(Xml.NAME)) != null) {
            _name = a.getValue();
        }
        if ((a = e.getAttribute(Xml.COMMENT)) != null) {
            _comment = OperationsXml.convertFromXmlComment(a.getValue());
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-locations.dtd.
     *
     * @return Contents in a JDOM Element
     */
    public Element store() {
        Element e = new Element(Xml.DIVISION);
        e.setAttribute(Xml.ID, getId());
        e.setAttribute(Xml.NAME, getName());
        e.setAttribute(Xml.COMMENT, getComment());
        return e;
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(LocationManagerXml.class).setDirty(true);
        firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(Division.class);
}
