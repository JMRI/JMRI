package jmri;

import org.jdom2.Element;

/**
 * BasicRosterEntry represents a single element in a locomotive roster, including
 * information on how to locate it from decoder information.
 * <p>
 * The roster interface is too allow access to the Basic details of a Roster
 * entry in the jmri.jmrit.roster package from the jmri.jmrix package.
 * <p>
 * Primarily this only deals as a method to provide other packages with access
 * to the information rather than being able set or create items. However access
 * to set and get Attributes is allowed so that attributes like running duration
 * can be recorded, with the store option also avaiable.
 * <p>
 * This interface should probably be called RosterEntry, but this would result in another class 
 * with the same name; refactoring of that class would be a big job and would potentially cause issue
 * with users scripts etc.
 * <p>
 * For Full read/write and creation of RosterEntries use the @link
 * jmri.jmrit.roster.RosterEntry
 * <p>
 * All properties, including the "Attributes", are bound.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2004, 2005, 2009
 * @author Kevin Dickerson Copyright 2012
 * @see jmri.jmrit.roster.RosterEntry
 *
 */
public interface BasicRosterEntry {

    public String getId();

    public String getDccAddress();

    public boolean isLongAddress();

    public DccLocoAddress getDccLocoAddress();

    public String getShuntingFunction();

    public void setOpen(boolean boo);

    public boolean isOpen();

    public void putAttribute(String key, String value);

    public String getAttribute(String key);

    public void deleteAttribute(String key);

    public String[] getAttributeList();

    public int getMaxSpeedPCT();

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed schema in
     * xml/schema/locomotive-config.xsd.
     *
     * @return Contents in a JDOM Element
     */
    public Element store();

    public String titleString();

    @Override
    public String toString();

    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

}
