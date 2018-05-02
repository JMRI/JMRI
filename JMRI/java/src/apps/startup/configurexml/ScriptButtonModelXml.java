package apps.startup.configurexml;

import apps.StartupActionsManager;
import apps.startup.ScriptButtonModel;
import apps.startup.StartupModel;
import java.io.FileNotFoundException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.util.FileUtil;
import jmri.util.prefs.InitializationException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence for {@link apps.startup.ScriptButtonModel} objects and
 * set the defined {@link jmri.Route} during application start.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Ken Cameron Copyright: 2014(c)
 * @author Randall Wood (c) 2016
 * @see apps.startup.TriggerRouteModelFactory
 */
public class ScriptButtonModelXml extends AbstractXmlAdapter {

    private final static Logger log = LoggerFactory.getLogger(ScriptButtonModelXml.class);

    public ScriptButtonModelXml() {
    }

    /**
     * Default implementation for storing the model contents
     *
     * @param o Object to store, of type PerformActonModel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        Element element = new Element("perform"); // NOI18N
        element.setAttribute("name", ((StartupModel) o).getName()); // NOI18N
        element.setAttribute("type", "Button"); // NOI18N
        element.setAttribute("class", this.getClass().getName()); // NOI18N
        Element property = new Element("property"); // NOI18N
        property.setAttribute("name", "script"); // NOI18N
        property.setAttribute("value", FileUtil.getPortableFilename(((ScriptButtonModel) o).getScript()));
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
        // Should the script engines be pre-loaded here?
        boolean result = false;
        ScriptButtonModel model = new ScriptButtonModel();
        model.setName(shared.getAttribute("name").getValue()); // NOI18N
        for (Element child : shared.getChildren("property")) { // NOI18N
            if (child.getAttributeValue("name").equals("script") // NOI18N
                    && child.getAttributeValue("value") != null) { // NOI18N
                String script = child.getAttributeValue("value"); // NOI18N
                try {
                    model.setScript(FileUtil.getFile(script));
                    result = true;
                } catch (FileNotFoundException ex) {
                    model.addException(new InitializationException(
                            Bundle.getMessage(Locale.ENGLISH, "ScriptButtonModel.ScriptNotFound", script),
                            Bundle.getMessage("ScriptButtonModel.ScriptNotFound", script),
                            ex));
                    log.error("Unable to create button for script {}", script);
                }
            }
        }

        // store the model
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
}
