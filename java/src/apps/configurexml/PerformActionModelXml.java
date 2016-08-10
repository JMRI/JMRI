package apps.configurexml;

import apps.PerformActionModel;
import apps.StartupActionsManager;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;
import jmri.util.ConnectionNameFromSystemName;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of PerformActionModel objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
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
        log.debug("Invoke Action from {}", className);
        try {
            Action action = (Action) Class.forName(className).newInstance();
            if (SystemConnectionAction.class.isAssignableFrom(action.getClass())) {
                SystemConnectionMemo memo = ConnectionNameFromSystemName.getSystemConnectionMemoFromSystemPrefix(model.getSystemPrefix());
                if (memo != null) {
                    ((SystemConnectionAction) action).setSystemConnectionMemo(memo);
                } else {
                    log.error("Connection {} does not exist. Cannot be assigned to action {}", model.getSystemPrefix(), className);
                    result = false;
                }
            }
            action.actionPerformed(new ActionEvent("prefs", 0, ""));
        } catch (ClassNotFoundException ex1) {
            log.error("Could not find specified class: {}", className);
            result = false;
        } catch (IllegalAccessException ex2) {
            log.error("Unexpected access exception for  class: " + className, ex2);
            result = false;
        } catch (InstantiationException ex3) {
            log.error("Could not instantiate specified class: " + className, ex3);
            result = false;
        } catch (Exception ex4) {
            log.error("Error while performing startup action for class: " + className, ex4);
            ex4.printStackTrace();
            result = false;
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
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(PerformActionModelXml.class.getName());

}
