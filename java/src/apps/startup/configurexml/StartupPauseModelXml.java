package apps.startup.configurexml;

import apps.startup.StartupPauseModel;

import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.util.startup.StartupActionsManager;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML persistence for {@link apps.startup.StartupPauseModel} objects.
 *
 * @author Randall Wood (c) 2016
 * @see apps.startup.StartupPauseFactory
 */
public class StartupPauseModelXml extends AbstractXmlAdapter {

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StartupPauseModelXml.class);

    public StartupPauseModelXml() {
    }

    @Override
    public Element store(Object o) {
        Element element = new Element("perform"); // NOI18N
        element.setAttribute("name", "Pause");
        element.setAttribute("type", "Pause");
        element.setAttribute("enabled", ((StartupPauseModel) o).isEnabled() ? "yes" : "no");
        element.setAttribute("class", this.getClass().getName());
        Element property = new Element("property"); // NOI18N
        property.setAttribute("name", "delay"); // NOI18N
        property.setAttribute("value", Integer.toString(((StartupPauseModel) o).getDelay()));
        element.addContent(property);
        return element;
    }

    @Override
    public boolean loadDeferred() {
        return true;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = false;
        StartupPauseModel model = new StartupPauseModel();

        Attribute enabled = shared.getAttribute("enabled");
        if (enabled != null) {
            model.setEnabled("yes".equals(enabled.getValue()));
        } else {
            model.setEnabled(true);
        }

        int delay = 0;
        for (Element child : shared.getChildren("property")) { // NOI18N
            if (child.getAttributeValue("name").equals("delay") // NOI18N
                    && child.getAttributeValue("value") != null) { // NOI18N
                delay = Integer.parseInt(child.getAttributeValue("value")); // NOI18N
            }
        }
        if (delay != 0) {
            model.setDelay(delay);
        }

        // store the model
        InstanceManager.getDefault(StartupActionsManager.class).addAction(model);
        return result;
    }

}
