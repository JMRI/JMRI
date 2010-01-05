package apps.configurexml;

import apps.Apps;
import apps.CreateButtonModel;

import javax.swing.Action;
import javax.swing.JButton;

import org.jdom.Element;

/**
 * Handle XML persistance of CreateButtonModel objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.11 $
 * @see apps.CreateButtonPanel
 */
public class CreateButtonModelXml extends jmri.configurexml.AbstractXmlAdapter {

    public CreateButtonModelXml() {
    }

    /**
     * Default implementation for storing the model contents
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
     * Create object from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
    	boolean result = true;
        String className = e.getAttribute("name").getValue();
        log.debug("Invoke Action from"+className);
        try {
            Action action = (Action)Class.forName(className).newInstance();
            if (Apps.buttonSpace()!=null) {
                // Complicated construct to get around no JButton(Action)
                // ctor in Java 1.1.8
                JButton b = new JButton((String)action.getValue(Action.NAME));
                b.addActionListener(action);
                Apps.buttonSpace().add(b);
            }
        } catch (ClassNotFoundException ex1) {
            log.error("Could not find specified class: "+className);
            result = false;
        } catch (IllegalAccessException ex2) {
            log.error("Unexpected access exception: "+ex2);
            result = false;
        } catch (InstantiationException ex3) {
            log.error("Could not instantiate specified class: "+className);
            ex3.printStackTrace();
            System.out.println(ex3);
            result = false;
        } catch (Exception ex4) {
            log.error("Error while performing startup action: "+ex4);
            ex4.printStackTrace();
            result = false;
        }
        CreateButtonModel m = new CreateButtonModel();
        m.setClassName(className);
        CreateButtonModel.rememberObject(m);
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CreateButtonModelXml.class.getName());

}