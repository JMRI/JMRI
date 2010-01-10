// SignalMastIconXml.java

package jmri.jmrit.display.configurexml;

import jmri.SignalMast;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.SignalMastIcon;
import jmri.util.NamedBeanHandle;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.SignalMastIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2010
 * @version $Revision: 1.1 $
 */
public class SignalMastIconXml extends PositionableLabelXml {

    public SignalMastIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SignalMastIcon
     * @param o Object to store, of type SignalMastIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        SignalMastIcon p = (SignalMastIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("signalmasticon");
        
        element.setAttribute("signalmast", ""+p.getPName());
        storeCommonAttributes(p, element);
        element.setAttribute("class", "jmri.jmrit.display.configurexml.SignalMastIconXml");
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a SignalMastIcon, then add
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        String className = o.getClass().getName();
		int lastDot = className.lastIndexOf(".");
		PanelEditor pe = null;
		LayoutEditor le = null;
        SignalMastIcon l;
        String name;
        
		String shortClass = className.substring(lastDot+1,className.length());
		if (shortClass.equals("PanelEditor")) {
			pe = (PanelEditor) o;
            l = new SignalMastIcon();
            loadCommonAttributes(l, PanelEditor.SIGNALS.intValue(), element);
		}
		else if (shortClass.equals("LayoutEditor")) {
			le = (LayoutEditor) o;
            l = new SignalMastIcon(le);
            loadCommonAttributes(l, LayoutEditor.SIGNALS.intValue(), element);
		}
		else {
			log.error("Unrecognizable class - "+className);
            l = new SignalMastIcon();
		}

        Attribute attr = element.getAttribute("signalmast"); 
        if (attr == null) {
            log.error("incorrect information for signal mast; must use signalmast name");
            return;
        } else {
            name = attr.getValue();
        }
        
        SignalMast sh = jmri.InstanceManager.signalMastManagerInstance().getSignalMast(name);

        if (sh != null) {
            l.setSignalMast(new NamedBeanHandle<SignalMast>(name, sh));
        } else {
            log.error("SignalMast named '"+attr.getValue()+"' not found.");
            return;
        }
        
        int rotation = 0;
        try {
            attr = element.getAttribute("rotate");
            rotation = attr.getIntValue();
        } catch (org.jdom.DataConversionException e){
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
                
        if (pe!=null){
            pe.putLabel(l);
        }
        else if (le!=null){
            //l.displayState();
            //le.putSignal(l);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalMastIconXml.class.getName());

}
