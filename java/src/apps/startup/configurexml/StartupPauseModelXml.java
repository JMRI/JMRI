package apps.startup.configurexml;

import apps.StartupActionsManager;
import apps.startup.StartupPauseModel;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence for {@link apps.startup.StartupPauseModel} objects.
 *
 * @author Randall Wood (c) 2016
 * @see apps.startup.StartupPauseFactory
 */
public class StartupPauseModelXml extends AbstractXmlAdapter {

    private final static Logger log = LoggerFactory.getLogger(StartupPauseModelXml.class);

    public StartupPauseModelXml() {
    }

    @Override
    public Element store(Object o) {
        Element element = new Element("perform"); // NOI18N
        element.setAttribute("name", "Pause");
        element.setAttribute("type", "Pause");
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

    @Override
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }

}
