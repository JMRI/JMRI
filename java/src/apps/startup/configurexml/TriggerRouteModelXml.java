package apps.startup.configurexml;

import apps.startup.TriggerRouteModel;

import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.util.startup.StartupActionsManager;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML persistence for {@link apps.startup.TriggerRouteModel} objects
 * and set the defined {@link jmri.Route} during application start.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Ken Cameron Copyright: 2014(c)
 * @author Randall Wood (c) 2016
 * @see apps.startup.TriggerRouteModelFactory
 */
public class TriggerRouteModelXml extends AbstractXmlAdapter {

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TriggerRouteModelXml.class);

    public TriggerRouteModelXml() {
    }

    /**
     * Default implementation for storing the model contents
     *
     * @param o Object to store, of type PerformActonModel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element e = new Element("perform"); // NOI18N
        e.setAttribute("name", ((TriggerRouteModel) o).getName());
        e.setAttribute("type", "Action");
        e.setAttribute("enabled", ((TriggerRouteModel) o).isEnabled() ? "yes" : "no");
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
        String userName = shared.getAttribute("name").getValue();

        TriggerRouteModel m = new TriggerRouteModel();
        m.setUserName(userName);

        Attribute enabled = shared.getAttribute("enabled");
        if (enabled != null) {
            m.setEnabled("yes".equals(enabled.getValue()));
        } else {
            m.setEnabled(true);
        }

        // store the model
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

}
