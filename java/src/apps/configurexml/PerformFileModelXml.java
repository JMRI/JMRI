package apps.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apps.PerformFileModel;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.util.FileUtil;

import java.io.File;

import org.jdom.Element;

/**
 * Handle XML persistance of PerformFileModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Ken Cameron Copyright: 2014(c)
 * @version $Revision$
 * @see apps.PerformFilePanel
 */
public class PerformFileModelXml extends jmri.configurexml.AbstractXmlAdapter {

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

        e.setAttribute("name", FileUtil.getPortableFilename(g.getFileName()));
        e.setAttribute("type", "XmlFile");
        e.setAttribute("class", this.getClass().getName());
        return e;
    }

    /**
     * Object should be loaded after basic GUI constructed
     * @return true to defer loading
     * @see jmri.configurexml.AbstractXmlAdapter#loadDeferred()
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     */
    @Override
    public boolean loadDeferred() {
        return true;
    }

    /**
     * Create object from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) throws JmriException {
    	boolean result = true;
        String fileName = FileUtil.getAbsoluteFilename(e.getAttribute("name").getValue());
        log.info("Load file "+fileName);

        // load the file
        File file = new File(fileName);
        result = InstanceManager.configureManagerInstance().load(file);

        // leave an updated object around
        PerformFileModel m = new PerformFileModel();
        m.setFileName(fileName);
        PerformFileModel.rememberObject(m);
        jmri.InstanceManager.configureManagerInstance().registerPref(new apps.PerformFilePanel());
        return result;
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
    static Logger log = LoggerFactory.getLogger(PerformFileModelXml.class.getName());

}
