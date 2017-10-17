package apps.configurexml;

import apps.PerformScriptModel;
import apps.StartupActionsManager;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of PerformScriptModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Ken Cameron Copyright: Copyright (c) 2014
 * @see apps.startup.PerformScriptModelFactory
 */
public class PerformScriptModelXml extends jmri.configurexml.AbstractXmlAdapter {

    public PerformScriptModelXml() {
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
        PerformScriptModel g = (PerformScriptModel) o;

        e.setAttribute("name", FileUtil.getPortableFilename(g.getFileName()));
        e.setAttribute("type", "ScriptFile");
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
        String fileName = shared.getAttribute("name").getValue();
        fileName = FileUtil.getAbsoluteFilename(fileName);
        PerformScriptModel m = new PerformScriptModel();
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
    private final static Logger log = LoggerFactory.getLogger(PerformScriptModelXml.class);

}
