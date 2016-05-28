package apps.configurexml;

import apps.PerformActionModel;
import apps.StartupActionsManager;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.InstanceManager;
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
        Element e = new Element("perform");
        PerformActionModel g = (PerformActionModel) o;

        e.setAttribute("name", g.getClassName());
        e.setAttribute("type", "Action");
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
        String className = shared.getAttribute("name").getValue();
        // rename MiniServerAction to WebServerAction
        if (className.equals("jmri.web.miniserver.MiniServerAction")) {
            className = "jmri.web.server.WebServerAction";
            log.debug("Updating MiniServerAction to WebServerAction");
        }
        log.debug("Invoke Action from {}", className);
        try {
            Action action = (Action) Class.forName(className).newInstance();
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
        PerformActionModel m = new PerformActionModel();
        m.setClassName(className);
        InstanceManager.getDefault(StartupActionsManager.class).addAction(m);
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
