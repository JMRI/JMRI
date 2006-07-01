package jmri.jmrit.symbolicprog.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.symbolicprog.ProgrammerConfigPane;
import org.jdom.Element;

/**
 * Handle XML persistance of symbolic programmer default values.
 * <P>
 * This class is named as being the persistant form of the
 * ProgrammerConfigPane class, but there's no object of that
 * form created when this is read back.  Instead, this persists static members of the
 * symbolicprog.CombinedLocoSelPane class.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 */
public class ProgrammerConfigPaneXml implements XmlAdapter {

    public ProgrammerConfigPaneXml() {
    }

    /**
     * Default implementation for storing the static contents
     * @param o Object to store, of type PositionableLabel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        ProgrammerConfigPane p = (ProgrammerConfigPane) o;
        if (p.getSelectedItem()==null) return null;  // nothing to write!
        Element programmer = new Element("programmer");
        programmer.addAttribute("defaultFile", p.getSelectedItem());
        programmer.addAttribute("verifyBeforeWrite", "no");
        programmer.addAttribute("class", this.getClass().getName());
        return programmer;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element element) {
        if (log.isDebugEnabled()) log.debug("set programmer default file: "+element.getAttribute("defaultFile").getValue());
        jmri.jmrit.symbolicprog.CombinedLocoSelPane.setDefaultProgFile(element.getAttribute("defaultFile").getValue());
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        jmri.jmrit.symbolicprog.CombinedLocoSelPane.setDefaultProgFile(element.getAttribute("defaultFile").getValue());
    }
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ProgrammerConfigPaneXml.class.getName());

}