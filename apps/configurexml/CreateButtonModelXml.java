package apps.configurexml;

import apps.Apps;
import apps.CreateButtonModel;

import jmri.configurexml.XmlAdapter;
import javax.swing.Action;
import javax.swing.JButton;

import org.jdom.Element;

/**
 * Handle XML persistance of CreateButtonModel objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 * @see apps.CreateButtonPanel
 */
public class CreateButtonModelXml implements XmlAdapter {

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

        e.addAttribute("name", g.getClassName());
        e.addAttribute("type", "Action");
        e.addAttribute("class", this.getClass().getName());
        return e;
    }

    /**
     * Create object from XML file
     * @param element Top level Element to unpack.
      */
    public void load(Element e) {
        String className = e.getAttribute("name").getValue();
        log.debug("Invoke Action from"+className);
        try {
            Action action = (Action)Class.forName(className).newInstance();
            if (Apps.buttonSpace()!=null) {
                Apps.buttonSpace().add(new JButton(action));
            }
        } catch (ClassNotFoundException ex1) {
            log.error("Could not find specified class: "+className);
        } catch (IllegalAccessException ex2) {
            log.error("Unexpected access exception: "+ex2);
        } catch (InstantiationException ex3) {
            log.error("Could not instantiate specified class: "+className);
        } catch (Exception ex4) {
            log.error("Error while performing startup action: "+ex4);
            ex4.printStackTrace();
        }
        CreateButtonModel m = new CreateButtonModel();
        m.setClassName(className);
        CreateButtonModel.rememberObject(m);
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
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CreateButtonModelXml.class.getName());

}