package apps.configurexml;

import apps.Apps;
import apps.CreateButtonModel;
import apps.StartupActionsManager;
import apps.gui3.Apps3;
import javax.swing.Action;
import javax.swing.JButton;
import jmri.InstanceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistance of CreateButtonModel objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @see apps.startup.CreateButtonModelFactory
 */
public class CreateButtonModelXml extends jmri.configurexml.AbstractXmlAdapter {

    public CreateButtonModelXml() {
    }

    /**
     * Default implementation for storing the model contents
     *
     * @param o Object to store, of type CreateButtonModel
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element e = new Element("perform");
        CreateButtonModel g = (CreateButtonModel) o;

        e.setAttribute("name", g.getClassName());
        e.setAttribute("type", "Button");
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
        log.debug("Invoke Action from {}", className);
        try {
            Action action = (Action) Class.forName(className).newInstance();
            if (Apps.buttonSpace() != null) {
                JButton b = new JButton(action);
                Apps.buttonSpace().add(b);
            } else if (Apps3.buttonSpace() != null) {
                JButton b = new JButton(action);
                Apps3.buttonSpace().add(b);
            }
        } catch (ClassNotFoundException ex1) {
            log.error("Could not find specified class: {}", className);
            result = false;
        } catch (IllegalAccessException ex2) {
            log.error("Unexpected access exception for class: {}", className, ex2);
            result = false;
        } catch (InstantiationException ex3) {
            log.error("Could not instantiate specified class: {}", className, ex3);
            result = false;
        } catch (Exception ex4) {
            log.error("Exception while performing startup action for class: {}", className, ex4);
            result = false;
        }
        CreateButtonModel m = new CreateButtonModel();
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
    private final static Logger log = LoggerFactory.getLogger(CreateButtonModelXml.class.getName());

}
