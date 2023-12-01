package jmri.util.plugin.configurexml;

import jmri.util.plugin.LoadPluginModel;

import jmri.util.startup.StartupActionsManager;
import jmri.InstanceManager;
import jmri.util.FileUtil;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML persistence of LoadPluginModel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Ken Cameron Copyright: Copyright (c) 2014
 * @author Daniel Bergqvist (C) 2023
 * @see jmri.util.plugin.LoadPluginModelFactory
 */
public class LoadPluginModelXml extends jmri.configurexml.AbstractXmlAdapter {

    public LoadPluginModelXml() {
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
        LoadPluginModel g = (LoadPluginModel) o;

        e.setAttribute("name", FileUtil.getPortableFilename(g.getFileName()));
        e.setAttribute("type", "PluginFile");
        e.setAttribute("enabled", g.isEnabled() ? "yes" : "no");
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
        LoadPluginModel m = new LoadPluginModel();

        Attribute enabled = shared.getAttribute("enabled");
        if (enabled != null) {
            m.setEnabled("yes".equals(enabled.getValue()));
        } else {
            m.setEnabled(true);
        }

        m.setFileName(fileName);
        InstanceManager.getDefault(StartupActionsManager.class).addAction(m);
        return result;
    }

    // initialize logging
//    private final static Logger log = LoggerFactory.getLogger(LoadPluginModelXml.class);

}
