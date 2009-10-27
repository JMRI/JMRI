package jmri.jmrit.display.configurexml;

import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.ReporterIcon;

import org.jdom.Element;

/**
 * Handle configuration for display.ReporterIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 * @version $Revision: 1.12 $
 */
public class ReporterIconXml extends PositionableLabelXml {

    public ReporterIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * ReporterIcon
     * @param o Object to store, of type ReporterIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        ReporterIcon p = (ReporterIcon)o;

        Element element = new Element("reportericon");

        // include contents
        element.setAttribute("reporter", p.getReporter().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));

        storeTextInfo(p, element);
        
        element.setAttribute("class", "jmri.jmrit.display.configurexml.ReporterIconXml");

        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
        // get object class and determine editor being used
		String className = o.getClass().getName();
		int lastDot = className.lastIndexOf(".");
		PanelEditor pe = null;
		LayoutEditor le = null;
		String shortClass = className.substring(lastDot+1,className.length());
		if (shortClass.equals("PanelEditor")) {
			pe = (PanelEditor) o;
		}
		else if (shortClass.equals("LayoutEditor")) {
			le = (LayoutEditor) o;
		}
		else {
			log.error("Unrecognizable class - "+className);
		}
        ReporterIcon l = new ReporterIcon();

        loadTextInfo(l, element);

        l.setReporter(jmri.InstanceManager.reporterManagerInstance().getReporter(
            element.getAttribute("reporter").getValue()));

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x,y);

        // find display level
        int level; 
        if(pe!=null)
            level = PanelEditor.REPORTERS.intValue();
        else
            level = LayoutEditor.LABELS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        if(pe!=null)
            pe.putLabel(l);
        else
            le.putLabel(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ReporterIconXml.class.getName());
}