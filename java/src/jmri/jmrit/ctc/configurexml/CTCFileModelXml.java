package jmri.jmrit.ctc.configurexml;

import apps.StartupActionsManager;
import jmri.InstanceManager;
import jmri.jmrit.ctc.CTCFileModel;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of PerformFileModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Ken Cameron Copyright: 2014(c)
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * @see apps.startup.PerformFileModelFactory
 */
public class CTCFileModelXml extends jmri.configurexml.AbstractXmlAdapter {
    private final static Logger log = LoggerFactory.getLogger(CTCFileModelXml.class);
    public CTCFileModelXml() {}

    /**
     * Default implementation for storing the model contents
     *
     * @param o Object to store, of type PerformActonModel
     * @return Element containing the complete info
     */
    @Override public Element store(Object o) {
        Element e = new Element("ctc");
        e.setAttribute("name", FileUtil.getPortableFilename(((CTCFileModel)o).getFileName()));
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
    @Override public boolean loadDeferred() { return true; }

    @Override public boolean load(Element shared, Element perNode) {
        boolean result = true;
        String fileName = FileUtil.getAbsoluteFilename(shared.getAttribute("name").getValue());
        CTCFileModel m = new CTCFileModel();
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
    @Override public void load(Element element, Object o) { log.error("Unexpected call of load(Element, Object)"); }
}
