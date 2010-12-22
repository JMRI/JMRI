package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MultiSensorIcon;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.List;

/**
 * Handle configuration for display.MultiSensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.23 $
 */
public class MultiSensorIconXml extends PositionableLabelXml {

    public MultiSensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * MultiSensorIcon
     * @param o Object to store, of type MultiSensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        MultiSensorIcon p = (MultiSensorIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("multisensoricon");
        storeCommonAttributes(p, element);

        element.setAttribute("updown", p.getUpDown()?"true":"false");

        for (int i = 0; i < p.getNumEntries(); i++) {
            Element e = storeIcon("active", p.getSensorIcon(i));
            e.setAttribute("sensor", p.getSensorName(i));
            element.addContent(e);
        }
        element.addContent(storeIcon("inactive", p.getInactiveIcon()));
        element.addContent(storeIcon("unknown", p.getUnknownIcon()));
        element.addContent(storeIcon("inconsistent", p.getInconsistentIcon()));
        
        element.setAttribute("class", "jmri.jmrit.display.configurexml.MultiSensorIconXml");
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  an Editor an Object
     */
    @SuppressWarnings("unchecked")
	public void load(Element element, Object o) {
		Editor pe = (Editor)o;
        MultiSensorIcon l = new MultiSensorIcon(pe);
        // create the objects
        int rotation = 0;
        try {
            rotation = element.getAttribute("rotate").getIntValue();
        } catch (org.jdom.DataConversionException e) {
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        
        loadSensorIcon("inactive", rotation, l, element);
        loadSensorIcon("unknown", rotation, l,element);
        loadSensorIcon("inconsistent", rotation, l,element);
        Attribute a = element.getAttribute("updown");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setUpDown(true);
        else
            l.setUpDown(false);

        // get the icon pairs & load
        List<Element> items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            if (item.getAttribute("sensor")!=null) {
                String sensor = item.getAttribute("sensor").getValue();
                NamedIcon icon;
                if (item.getAttribute("url")!=null) {
                    String name = item.getAttribute("url").getValue();
                    icon = NamedIcon.getIconByName(name);
                    if (icon==null) {
                        return;
                    }
                    try {
                        int deg = 0;
                        a = item.getAttribute("degrees");
                        if (a!=null) {
                            deg = a.getIntValue();
                            double scale = 1.0;
                            a =  item.getAttribute("scale");
                            if (a!=null)
                            {
                                scale = item.getAttribute("scale").getDoubleValue();
                            }
                            icon.setLoad(deg, scale, l);
                        }
                        if (deg==0) {
                            a = item.getAttribute("rotate");
                            if (a!=null) {
                                rotation = a.getIntValue();
                                icon.setRotation(rotation, l);
                            }
                        }
                    } catch (org.jdom.DataConversionException dce) {}
                } else {
                    String name = item.getAttribute("icon").getValue();
                    icon = NamedIcon.getIconByName(name);
                    if (icon==null) {
                        return;
                    }
                    if (rotation!=0) icon.setRotation(rotation, l);
                }
              
                l.addEntry(sensor, icon);
            }
        }
        pe.putItem(l);
        // load individual item's option settings after editor has set its global settings
		loadCommonAttributes(l, Editor.SENSORS, element);
    }
    
    private void loadSensorIcon(String state, int rotation, MultiSensorIcon l, Element element){
        NamedIcon icon = loadIcon(l,state, element);
        if (icon==null){
            if (element.getAttribute(state) != null) {
                String name;
                name = element.getAttribute(state).getValue();
                icon = NamedIcon.getIconByName(name);
                if (icon==null) {
                    return;
                }
                icon.setRotation(rotation, l);
            }
            else log.warn("did not locate " + state + " for Multisensor icon file");
        }
        if (icon!=null) {
            if (state.equals("inactive")) l.setInactiveIcon(icon);
            else if (state.equals("unknown")) l.setUnknownIcon(icon);
            else if (state.equals("inconsistent")) l.setInconsistentIcon(icon);
        }
    }
    

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MultiSensorIconXml.class.getName());

}