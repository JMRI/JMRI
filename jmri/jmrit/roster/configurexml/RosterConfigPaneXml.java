package jmri.jmrit.roster.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.roster.RosterConfigPane;
import org.jdom.Element;
import jmri.jmrit.roster.*;

/**
 * Handle XML persistance of Roster default values.
 * <P>
 * This class is named as being the persistant form of the
 * RosterConfigPane class, but there's no object of that
 * form created when this is read back.  Instead, this persists static members of the
 * roster.Roster class.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
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
        if (p.getSelectedItem()==null) return null;  // nothing to write!
        Element roster = new Element("roster");
        roster.addAttribute("directory", p.getSelectedItem());
        roster.addAttribute("class", this.getClass().getName());
        return roster;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element element) {
        if (log.isDebugEnabled()) log.debug("set roster location: "+element.getAttribute("directory").getValue());
        Roster.setFileLocation(element.getAttribute("directory").getValue());
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        Roster.setFileLocation(element.getAttribute("directory").getValue());
    }
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterConfigPaneXml.class.getName());

}