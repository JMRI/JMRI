package apps.configurexml;

import apps.PerformScriptModel;

import jmri.InstanceManager;
import jmri.configurexml.XmlAdapter;

import org.jdom.Element;

/**
 * Handle XML persistance of PerformScriptModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.2 $
 * @see apps.PerformScriptPanel
 */
public class PerformScriptModelXml implements XmlAdapter {

    public PerformScriptModelXml() {
    }

    /**
     * Default implementation for storing the model contents
     * @param o Object to store, of type PerformActonModel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element e = new Element("perform");
        PerformScriptModel g = (PerformScriptModel) o;

        e.addAttribute("name", g.getFileName());
        e.addAttribute("type", "ScriptFile");
        e.addAttribute("class", this.getClass().getName());
        return e;
    }

    /**
     * Create object from XML file
     * @param e Top level Element to unpack.
      */
    public void load(Element e) {
        String fileName = e.getAttribute("name").getValue();
        log.debug("Run file "+fileName);

        // run the script
        jmri.jmrit.jython.RunJythonScript.runScript(fileName);

        // leave an updated object around
        PerformScriptModel m = new PerformScriptModel();
        m.setFileName(fileName);
        PerformScriptModel.rememberObject(m);
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PerformScriptModelXml.class.getName());

}