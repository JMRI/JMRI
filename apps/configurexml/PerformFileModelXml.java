package apps.configurexml;

import apps.PerformFileModel;

import jmri.InstanceManager;
import jmri.configurexml.XmlAdapter;
import java.io.File;

import org.jdom.Element;

/**
 * Handle XML persistance of PerformFileModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.3 $
 * @see apps.PerformFilePanel
 */
public class PerformFileModelXml implements XmlAdapter {

    public PerformFileModelXml() {
    }

    /**
     * Default implementation for storing the model contents
     * @param o Object to store, of type PerformActonModel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element e = new Element("perform");
        PerformFileModel g = (PerformFileModel) o;

        e.addAttribute("name", g.getFileName());
        e.addAttribute("type", "XmlFile");
        e.addAttribute("class", this.getClass().getName());
        return e;
    }

    /**
     * Create object from XML file
     * @param e Top level Element to unpack.
      */
    public void load(Element e) {
        String fileName = e.getAttribute("name").getValue();
        log.debug("Load file "+fileName);

        // load the file
        File file = new File(fileName);
        InstanceManager.configureManagerInstance().load(file);

        // leave an updated object around
        PerformFileModel m = new PerformFileModel();
        m.setFileName(fileName);
        PerformFileModel.rememberObject(m);
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
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PerformFileModelXml.class.getName());

}