package apps.configurexml;

import apps.PerformActionModel;
import apps.StartupActionsManager;
import jmri.InstanceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of PerformActionModel objects.
 *
 * @author Bob Jacobsen Copyright (c) 2003
 * @see apps.startup.PerformActionModelFactory
 */
public class PerformActionModelXml extends jmri.configurexml.AbstractXmlAdapter {

    public PerformActionModelXml() {
    }

    /**
     * Default implementation for storing the model contents
     *
     * @param o Object to store, of type PerformActonModel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element element = new Element("perform");
        PerformActionModel g = (PerformActionModel) o;

        element.setAttribute("name", g.getClassName());
        element.setAttribute("type", "Action");
        element.setAttribute("class", this.getClass().getName());
        Element property = new Element("property"); // NOI18N
        property.setAttribute("name", "systemPrefix"); // NOI18N
        property.setAttribute("value", g.getSystemPrefix());
        element.addContent(property);
        return element;
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
        String className = shared.getAttribute("name").getValue();
        PerformActionModel model = new PerformActionModel();
        model.setClassName(className);
        for (Element child : shared.getChildren("property")) { // NOI18N
            if (child.getAttributeValue("name").equals("systemPrefix") // NOI18N
                    && child.getAttributeValue("value") != null) { // NOI18N
                model.setSystemPrefix(child.getAttributeValue("value")); // NOI18N
            }
        }
        InstanceManager.getDefault(StartupActionsManager.class).addAction(model);
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
    private final static Logger log = LoggerFactory.getLogger(PerformActionModelXml.class);

}
