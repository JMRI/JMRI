package jmri.jmrit.roster.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterConfigPane;
import jmri.jmrit.roster.RosterEntry;
import org.jdom.Element;

/**
 * Handle XML persistance of Roster default values.
 * <P>
 * This class is named as being the persistant form of the
 * RosterConfigPane class, but there's no object of that
 * form created when this is read back.  Instead, this persists static members of the
 * roster.Roster class.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.5 $
 */
public class RosterConfigPaneXml implements XmlAdapter {

    public RosterConfigPaneXml() {
    }

    /**
     * Default implementation for storing the static contents
     * @param o Object to store
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        RosterConfigPane p = (RosterConfigPane) o;
        // any reason to write?
        if  ( (p.getSelectedItem()==null || p.getSelectedItem().equals(""))
            && p.getDefaultOwner().equals("") )  return null;
        
        // create and write element
        Element roster = new Element("roster");
        if (p.getSelectedItem()!=null && !p.getSelectedItem().equals("")) 
            roster.setAttribute("directory", p.getSelectedItem());
        roster.setAttribute("class", this.getClass().getName());
        roster.setAttribute("ownerDefault", p.getDefaultOwner());
        return roster;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element element) {
        if (element.getAttribute("directory")!=null) {
            Roster.setFileLocation(element.getAttribute("directory").getValue());
            if (log.isDebugEnabled()) log.debug("set roster location (1): "+element.getAttribute("directory").getValue());
        }
        if (element.getAttribute("ownerDefault")!=null) RosterEntry.setDefaultOwner(element.getAttribute("ownerDefault").getValue());
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        if (log.isDebugEnabled()) log.debug("set roster location (2): "+element.getAttribute("directory").getValue());
        if (element.getAttribute("directory")!=null)
            Roster.setFileLocation(element.getAttribute("directory").getValue());
    }
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterConfigPaneXml.class.getName());

}