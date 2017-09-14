package jmri.jmrit.display.configurexml;

import java.util.List;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MultiSensorIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.MultiSensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class MultiSensorIconXml extends PositionableLabelXml {

    public MultiSensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a MultiSensorIcon
     *
     * @param o Object to store, of type MultiSensorIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        MultiSensorIcon p = (MultiSensorIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("multisensoricon");
        storeCommonAttributes(p, element);

        element.setAttribute("updown", p.getUpDown() ? "true" : "false");

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

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor an Object
     */
    @Override
    public void load(Element element, Object o) {
        Editor pe = (Editor) o;
        MultiSensorIcon l = new MultiSensorIcon(pe);
        // create the objects
        int rotation = 0;
        try {
            rotation = element.getAttribute("rotate").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        NamedIcon icon = loadSensorIcon("inactive", rotation, l, element, pe);
        if (icon != null) {
            l.setInactiveIcon(icon);
        } else {
            return;
        }
        icon = loadSensorIcon("unknown", rotation, l, element, pe);
        if (icon != null) {
            l.setUnknownIcon(icon);
        } else {
            return;
        }
        icon = loadSensorIcon("inconsistent", rotation, l, element, pe);
        if (icon != null) {
            l.setInconsistentIcon(icon);
        } else {
            return;
        }

        Attribute a = element.getAttribute("updown");
        if ((a != null) && a.getValue().equals("true")) {
            l.setUpDown(true);
        } else {
            l.setUpDown(false);
        }

        // get the icon pairs & load
        List<Element> items = element.getChildren();
        for (int i = 0; i < items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            if (item.getAttribute("sensor") != null) {
                String sensor = item.getAttribute("sensor").getValue();
                if (item.getAttribute("url") != null) {
                    String name = item.getAttribute("url").getValue();
                    icon = NamedIcon.getIconByName(name);
                    if (icon == null) {
                        icon = pe.loadFailed("MultiSensor \"" + l.getNameString() + "\" ", name);
                        if (icon == null) {
                            log.error("MultiSensor \"" + l.getNameString() + "\" removed for url= " + name);
                            return;
                        }
                    }
                    try {
                        int deg = 0;
                        a = item.getAttribute("degrees");
                        if (a != null) {
                            deg = a.getIntValue();
                            double scale = 1.0;
                            a = item.getAttribute("scale");
                            if (a != null) {
                                scale = item.getAttribute("scale").getDoubleValue();
                            }
                            icon.setLoad(deg, scale, l);
                        }
                        if (deg == 0) {
                            a = item.getAttribute("rotate");
                            if (a != null) {
                                rotation = a.getIntValue();
                                icon.setRotation(rotation, l);
                            }
                        }
                    } catch (org.jdom2.DataConversionException dce) {
                    }
                } else {
                    String name = item.getAttribute("icon").getValue();
                    icon = NamedIcon.getIconByName(name);
                    if (icon == null) {
                        icon = pe.loadFailed("MultiSensor \"" + l.getNameString(), name);
                        if (icon == null) {
                            log.info("MultiSensor \"" + l.getNameString() + " removed for url= " + name);
                            return;
                        }
                    }
                    if (rotation != 0) {
                        icon.setRotation(rotation, l);
                    }
                }

                l.addEntry(sensor, icon);
            }
        }
        pe.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.SENSORS, element);
    }

    private NamedIcon loadSensorIcon(String state, int rotation, MultiSensorIcon l, Element element, Editor ed) {
        String msg = "MultiSensor \"" + l.getNameString() + "\": icon \"" + state + "\" ";
        NamedIcon icon = loadIcon(l, state, element, msg, ed);
        if (icon == null) {
            if (element.getAttribute(state) != null) {
                String iconName = element.getAttribute(state).getValue();
                icon = NamedIcon.getIconByName(iconName);
                if (icon == null) {
                    icon = ed.loadFailed(msg, iconName);
                    if (icon == null) {
                        log.info(msg + " removed for url= " + iconName);
                    }
                } else {
                    icon.setRotation(rotation, l);
                }
            } else {
                log.warn("did not locate " + state + " for Multisensor icon file");
            }
        }
        if (icon == null) {
            log.info("MultiSensor Icon \"" + l.getNameString() + "\": icon \"" + state + "\" removed");
        }
        return icon;
    }

    private final static Logger log = LoggerFactory.getLogger(MultiSensorIconXml.class);

}
