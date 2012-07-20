package apps.configurexml;

import apps.PerformActionModel;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.jdom.Element;

/**
 * Handle XML persistance of PerformActionModel objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 * @see apps.PerformActionPanel
 */
public class PerformActionModelXml extends jmri.configurexml.AbstractXmlAdapter {

    public PerformActionModelXml() {
    }

    /**
     * Default implementation for storing the model contents
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
     * @return true to defer loading
     * @see jmri.configurexml.AbstractXmlAdapter#loadDeferred()
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     */
    @Override
    public boolean loadDeferred() {
        return true;
    }

    /**
     * Create object from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
    	boolean result = true;
        String className = e.getAttribute("name").getValue();
        // rename MiniServerAction to WebServerAction
        if (className.equals("jmri.web.miniserver.MiniServerAction")) {
            className = "jmri.web.server.WebServerAction";
            log.debug("Updating MiniServerAction to WebServerAction");
        }
        log.debug("Invoke Action from"+className);
        try {
            Action action = (Action)Class.forName(className).newInstance();
            action.actionPerformed(new ActionEvent("prefs", 0, ""));
        } catch (ClassNotFoundException ex1) {
            log.error("Could not find specified class: "+className);
            result = false;
        } catch (IllegalAccessException ex2) {
            log.error("Unexpected access exception", ex2);
            result = false;
        } catch (InstantiationException ex3) {
            log.error("Could not instantiate specified class: "+className, ex3);
            result = false;
        } catch (Exception ex4) {
            log.error("Error while performing startup action", ex4);
            ex4.printStackTrace();
            result = false;
        }
        PerformActionModel m = new PerformActionModel();
        m.setClassName(className);
        PerformActionModel.rememberObject(m);
        jmri.InstanceManager.configureManagerInstance().registerPref(new apps.PerformActionPanel());
        return result;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PerformActionModelXml.class.getName());

}