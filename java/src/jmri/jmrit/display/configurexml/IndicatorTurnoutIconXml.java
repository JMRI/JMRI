package jmri.jmrit.display.configurexml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.Turnout;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.*;
import jmri.jmrit.logix.OBlock;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.IndicatorTurnoutIconXml objects.
 *
 * @author Pete Cressman Copyright: Copyright (c) 2010
 */
public class IndicatorTurnoutIconXml extends PositionableLabelXml {

    public IndicatorTurnoutIconXml() {
    }

    /**
     * Default implementation for storing the contents of a IndicatorTurnoutIcon
     *
     * @param o Object to store, of type IndicatorTurnoutIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        IndicatorTurnoutIcon p = (IndicatorTurnoutIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("indicatorturnouticon");
        storeCommonAttributes(p, element);

        NamedBeanHandle<Turnout> t = p.getNamedTurnout();
        if (t != null) {
            element.addContent(storeNamedBean("turnout", t));
        }
        NamedBeanHandle<OBlock> b = p.getNamedOccBlock();
        if (b != null) {
            element.addContent(storeNamedBean("occupancyblock", b));
            // additional oblock information for web server is extracted by ControlPanelServlet at runtime, not stored
        }
        NamedBeanHandle<Sensor> s = p.getNamedOccSensor();
        if (b == null && s != null) { // only write sensor if no OBlock
            element.addContent(storeNamedBean("occupancysensor", s));
        }

        Element elem = new Element("showTrainName");
        String show = "no";
        if (p.showTrain()) {
            show = "yes";
        }
        elem.addContent(show);
        element.addContent(elem);

        HashMap<String, HashMap<Integer, NamedIcon>> iconMaps = p.getIconMaps();
        Iterator<Entry<String, HashMap<Integer, NamedIcon>>> it = iconMaps.entrySet().iterator();
        Element el = new Element("iconmaps");
        String family = p.getFamily();
        if (family != null) {
            el.setAttribute("family", family);
        }
        while (it.hasNext()) {
            Entry<String, HashMap<Integer, NamedIcon>> ent = it.next();
            elem = new Element(ent.getKey());
            for (Entry<Integer, NamedIcon> entry : ent.getValue().entrySet()) {
                elem.addContent(storeIcon(p.getStateName(entry.getKey()), entry.getValue()));
            }
            el.addContent(elem);
        }
        element.addContent(el);

        elem = new Element("paths");
        ArrayList<String> paths = p.getPaths();
        if (paths != null) {
            for (String path : paths) {
                Element e = new Element("path");
                e.addContent(path);
                elem.addContent(e);

            }
            element.addContent(elem);
        }

        element.setAttribute("class", "jmri.jmrit.display.configurexml.IndicatorTurnoutIconXml");

        return element;
    }

    Element storeNamedBean(String elemName, NamedBeanHandle<?> nb) {
        Element elem = new Element(elemName);
        elem.addContent(nb.getName());
        return elem;
    }

    /**
     * Create a IndicatorTurnoutIcon, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        Editor p = (Editor) o;

        IndicatorTurnoutIcon l = new IndicatorTurnoutIcon(p);
        Element name = element.getChild("turnout");

        if (name == null) {
            log.error("incorrect information for turnout; must use turnout name");
        } else {
            l.setTurnout(name.getText());
        }
        Element elem = element.getChild("iconmaps");
        if (elem != null) {
            List<Element> maps = elem.getChildren();
            if (maps.size() > 0) {
                for (Element map : maps) {
                    String status = map.getName();
                    List<Element> states = map.getChildren();
                    for (Element state : states) {
                        String msg = "IndicatorTurnout \"" + l.getNameString() + "\" icon \"" + state.getName() + "\" ";
                        NamedIcon icon = loadIcon(l, state.getName(), map, msg, p);
                        if (icon != null) {
                            l.setIcon(status, state.getName(), icon);
                        } else {
                            log.info("{} removed for url= {}", msg, name);
                            return;
                        }
                    }
                }
            }
            Attribute attr = elem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
        }

        name = element.getChild("occupancyblock");
        if (name != null) {
            l.setOccBlock(name.getText());
        } else {        // we only wrote sensor if no OBlock, so assume sensor is empty
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
            ArrayList<String> paths = new ArrayList<>();
            List<Element> pth = elem.getChildren();
            for (Element value : pth) {
                paths.add(value.getText());
            }
            l.setPaths(paths);
        }

        l.updateSize();
        try {
            p.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }

    private final static Logger log = LoggerFactory.getLogger(IndicatorTurnoutIconXml.class);
}
