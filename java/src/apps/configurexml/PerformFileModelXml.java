package apps.configurexml;

import apps.PerformFileModel;
import apps.StartupActionsManager;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of PerformFileModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Ken Cameron Copyright: 2014(c)
 * @see apps.startup.PerformFileModelFactory
 */
public class PerformFileModelXml extends jmri.configurexml.AbstractXmlAdapter {

    public PerformFileModelXml() {
    }

    /**
     * Default implementation for storing the model contents
     *
     * @param o Object to store, of type PerformActonModel
     * @return Element containing the complete info
     */
    @Override
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
     *
     * @return true to defer loading
     * @see jmri.configurexml.AbstractXmlAdapter#loadDeferred()
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     */
    @Override
    public boolean loadDeferred() {
        return true;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        String fileName = FileUtil.getAbsoluteFilename(shared.getAttribute("name").getValue());
        PerformFileModel m = new PerformFileModel();
        m.setFileName(fileName);
        InstanceManager.getDefault(StartupActionsManager.class).addAction(m);
        return result;
    }

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       ignored
     */
    @Override
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(PerformFileModelXml.class);

}
