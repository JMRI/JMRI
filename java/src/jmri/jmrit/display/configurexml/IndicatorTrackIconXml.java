package jmri.jmrit.display.configurexml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.logix.OBlock;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.IndicatorTrackIconXml objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2010
 */
public class IndicatorTrackIconXml extends PositionableLabelXml {

    public IndicatorTrackIconXml() {
    }

    /**
     * Default implementation for storing the contents of a IndicatorTrackIcon
     *
     * @param o Object to store, of type IndicatorTrackIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        IndicatorTrackIcon p = (IndicatorTrackIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("indicatortrackicon");
        storeCommonAttributes(p, element);

        NamedBeanHandle<OBlock> b = p.getNamedOccBlock();
        if (b != null) {
            element.addContent(storeNamedBean("occupancyblock", b));
        }
        NamedBeanHandle<Sensor> s = p.getNamedOccSensor();
        if (b == null && s != null) {  // only write sensor if no OBlock, don't write double sensing
            element.addContent(storeNamedBean("occupancysensor", s));
        }
        /*
         s = p.getErrSensor();
         if (s!=null) {
         element.addContent(storeBean("errorsensor", s));
         }
         */
        Element elem = new Element("showTrainName");
        String show = "no";
        if (p.showTrain()) {
            show = "yes";
        }
        
        elem.addContent(show);
        element.addContent(elem);

        HashMap<String, NamedIcon> iconMap = p.getIconMap();
        Iterator<Entry<String, NamedIcon>> it = iconMap.entrySet().iterator();
        elem = new Element("iconmap");
        String family = p.getFamily();
        if (family != null) {
            elem.setAttribute("family", family);
        }
        while (it.hasNext()) {
            Entry<String, NamedIcon> entry = it.next();
            elem.addContent(storeIcon(entry.getKey(), entry.getValue()));
        }
        element.addContent(elem);

        elem = new Element("paths");
        ArrayList<String> paths = p.getPaths();
        if (paths != null) {
            for (int i = 0; i < paths.size(); i++) {
                Element e = new Element("path");
                e.addContent(paths.get(i));
                elem.addContent(e);

            }
            element.addContent(elem);
        }

        element.setAttribute("class", "jmri.jmrit.display.configurexml.IndicatorTrackIconXml");

        return element;
    }

    /*Element storeBean(String elemName, NamedBean nb) {
     Element elem = new Element(elemName);
     elem.addContent(nb.getSystemName());
     return elem;
     }*/
    static Element storeNamedBean(String elemName, NamedBeanHandle<?> nb) {
        Element elem = new Element(elemName);
        elem.addContent(nb.getName());
        return elem;
    }

    /**
     * Create a IndicatorTrackIcon, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor) o;

        IndicatorTrackIcon l = new IndicatorTrackIcon(p);

        Element elem = element.getChild("iconmap");
        if (elem != null) {
            List<Element> status = elem.getChildren();
            if (status.size() > 0) {
                for (int i = 0; i < status.size(); i++) {
                    String msg = "IndicatorTrack \"" + l.getNameString() + "\" icon \"" + status.get(i).getName() + "\" ";
                    NamedIcon icon = loadIcon(l, status.get(i).getName(), elem,
                            msg, p);
                    if (icon != null) {
                        l.setIcon(status.get(i).getName(), icon);
                    } else {
                        log.info(msg + " removed for url= " + status.get(i).getName());
                        return;
                    }
                }
            }
            Attribute attr = elem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
        }
        Element name = element.getChild("occupancyblock");
        if (name != null) {
            l.setOccBlock(name.getText());
        } else {        // only write sensor if no OBlock, don't write double sensing
            name = element.getChild("occupancysensor");
            if (name != null) {
                l.setOccSensor(name.getText());
            }            
        }

        l.setShowTrain(false);
        name = element.getChild("showTrainName");
        if (name != null) {
            if ("yes".equals(name.getText())) {
                l.setShowTrain(true);
            }
        }

        elem = element.getChild("paths");
        if (elem != null) {
            ArrayList<String> paths = new ArrayList<String>();
            List<Element> pth = elem.getChildren();
            for (int i = 0; i < pth.size(); i++) {
                paths.add(pth.get(i).getText());
            }
            l.setPaths(paths);
        }

        l.displayState(l.getStatus());
        l.updateSize();
        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTrackIconXml.class);
}
