package jmri.jmrit.display.configurexml;

import jmri.NamedBean;
import jmri.Sensor;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.logix.OBlock;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackIcon;

import org.jdom.Attribute;
import org.jdom.Element;
import java.util.List;

/**
 * Handle configuration for display.IndicatorTrackIconXml objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2010
 * @version $Revision: 1.1 $
 */
public class IndicatorTrackIconXml extends PositionableLabelXml {

    public IndicatorTrackIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * IndicatorTrackIcon
     * @param o Object to store, of type IndicatorTrackIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        IndicatorTrackIcon p = (IndicatorTrackIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("indicatortrackicon");
        storeCommonAttributes(p, element);

        OBlock b = p.getOccBlock();
        if (b!=null) {
            element.addContent(storeBean("occupancyblock", b));
        }
        Sensor s = p.getOccSensor();
        if (s!=null) {
            element.addContent(storeBean("occupancysensor", s));
        }
        s = p.getErrSensor();
        if (s!=null) {
            element.addContent(storeBean("errorsensor", s));
        }

        Hashtable<String, NamedIcon> iconMap = p.getIconMap();
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        Element elem = new Element("iconmap");
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            elem.addContent(storeIcon(entry.getKey(), entry.getValue()));
        }
        element.addContent(elem);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.IndicatorTrackIconXml");

        return element;
    }

    Element storeBean(String elemName, NamedBean nb) {
        Element elem = new Element(elemName);
        elem.addContent(nb.getSystemName());
        return elem;
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a IndicatorTrackIcon, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  Editor as an Object
     */
    @SuppressWarnings("null")
	public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor)o;

        IndicatorTrackIcon l = new IndicatorTrackIcon(p);
        
        Element name = element.getChild("occupancyblock");
        if (name!=null) {
            l.setOccBlock(name.getText());
        }
        name = element.getChild("occupancysensor");
        if (name!=null) {
            l.setOccSensor(name.getText());
        }
        name = element.getChild("errorsensor");
        if (name!=null) {
            l.setErrSensor(name.getText());
        }
        
        Element elem = element.getChild("iconmap");
        @SuppressWarnings("unchecked")
        List<Element>status = elem.getChildren();
        if (status.size()>0) {
            for (int i=0; i<status.size(); i++) {
                NamedIcon icon = loadIcon(l, status.get(i).getName(), elem);
                l.setIcon(status.get(i).getName(), icon);
            }
        }
            
        l.updateSize();
        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IndicatorTrackIconXml.class.getName());
}

