package jmri.jmrit.logixng.startup.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.logixng.startup.LogixNG_StartupPauseModel;
import jmri.util.startup.StartupActionsManager;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle XML persistence for {@link jmri.jmrit.logixng.startup.LogixNG_StartupPauseModel} objects.
 *
 * @author Randall Wood     (C) 2016
 * @author Daniel Bergqvist (C) 2026
 */
public class LogixNG_StartupPauseModelXml extends AbstractXmlAdapter {

    public LogixNG_StartupPauseModelXml() {
    }

    @Override
    public Element store(Object o) {
        String moduleName = ((LogixNG_StartupPauseModel) o).getModuleName();

        Element element = new Element("perform"); // NOI18N
        element.setAttribute("name", "Pause");
        element.setAttribute("type", "Pause");
        element.setAttribute("enabled", ((LogixNG_StartupPauseModel) o).isEnabled() ? "yes" : "no");
        element.setAttribute("class", this.getClass().getName());
        Element property = new Element("property"); // NOI18N
        property.setAttribute("name", "module"); // NOI18N
        property.setAttribute("value", moduleName != null ? moduleName : "");
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
        LogixNG_StartupPauseModel model = new LogixNG_StartupPauseModel();

        Attribute enabled = shared.getAttribute("enabled");
        if (enabled != null) {
            model.setEnabled("yes".equals(enabled.getValue()));
        } else {
            model.setEnabled(true);
        }

        for (Element child : shared.getChildren("property")) {
            if (child.getAttributeValue("name").equals("module")
                    && !child.getAttributeValue("value").isEmpty()) {
                model.setModuleName(child.getAttributeValue("value"));
            }
        }

        // store the model
        InstanceManager.getDefault(StartupActionsManager.class).addAction(model);
        return result;
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_StartupPauseModelXml.class);

}
