package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import java.awt.Color;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.ToolTip;
import jmri.jmrit.display.controlPanelEditor.shape.PositionableShape;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.PositionableShape objects
 *
 * @author Pete Cressman Copyright: Copyright (c) 2012
 */
public class PositionableShapeXml extends AbstractXmlAdapter {

    public PositionableShapeXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionableShape
     *
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PositionableShape p = (PositionableShape) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("PositionableShape");
        storeCommonAttributes(p, element);

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.shape.configurexml.PositionableShapeXml");
        return element;
    }

    /**
     * Default implementation for storing the common contents
     *
     * @param element Element in which contents are stored
     */
    public void storeCommonAttributes(PositionableShape p, Element element) {
        element.setAttribute("x", "" + p.getX());
        element.setAttribute("y", "" + p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.setAttribute("forcecontroloff", !p.isControlling() ? "true" : "false");
        element.setAttribute("hidden", p.isHidden() ? "yes" : "no");
        element.setAttribute("positionable", p.isPositionable() ? "true" : "false");
        element.setAttribute("showtooltip", p.showTooltip() ? "true" : "false");
        element.setAttribute("editable", p.isEditable() ? "true" : "false");
        ToolTip tip = p.getTooltip();
        String txt = tip.getText();
        if (txt != null) {
            Element elem = new Element("toolTip").addContent(txt);
            element.addContent(elem);
        }
        if (p.getDegrees() != 0) {
            element.setAttribute("degrees", "" + p.getDegrees());
        }

        Element elem = storeColor("lineColor", p.getLineColor());
        if (elem != null) {
            element.addContent(elem);
        }
        elem = storeColor("fillColor", p.getFillColor());
        if (elem != null) {
            element.addContent(elem);
        }
        element.setAttribute("lineWidth", "" + p.getLineWidth());

        NamedBeanHandle<Sensor> handle = p.getControlSensorHandle();
        if (handle != null) {
            element.setAttribute("controlSensor", handle.getName());
        }
        element.setAttribute("hideOnSensor", p.isHideOnSensor() ? "true" : "false");
        element.setAttribute("changeLevelOnSensor", String.valueOf(p.getChangeLevel()));
    }

    public Element storeColor(String name, Color c) {
        if (c == null) {
            return null;
        }
        Element elem = new Element(name);
        elem.setAttribute("red", "" + c.getRed());
        elem.setAttribute("green", "" + c.getGreen());
        elem.setAttribute("blue", "" + c.getBlue());
        elem.setAttribute("alpha", "" + c.getAlpha());
        return elem;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableShape, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor) o;
        PositionableShape ps = new PositionableShape(ed);

        ed.putItem(ps);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(ps, Editor.MARKERS, element);
    }

    public void loadCommonAttributes(PositionableShape ps, int defaultLevel, Element element) {
        int x = getInt(element, "x");
        int y = getInt(element, "y");
        ps.setLocation(x, y);

        ps.setDisplayLevel(getInt(element, "level"));

        Attribute a = element.getAttribute("hidden");
        if ((a != null) && a.getValue().equals("yes")) {
            ps.setHidden(true);
            ps.setVisible(false);
        }
        a = element.getAttribute("positionable");
        if ((a != null) && a.getValue().equals("true")) {
            ps.setPositionable(true);
        } else {
            ps.setPositionable(false);
        }

        a = element.getAttribute("showtooltip");
        if ((a != null) && a.getValue().equals("true")) {
            ps.setShowTooltip(true);
        } else {
            ps.setShowTooltip(false);
        }

        a = element.getAttribute("editable");
        if ((a != null) && a.getValue().equals("true")) {
            ps.setEditable(true);
        } else {
            ps.setEditable(false);
        }

        Element elem = element.getChild("toolTip");
        if (elem != null) {
            ToolTip tip = ps.getTooltip();
            if (tip != null) {
                tip.setText(elem.getText());
            }
        }
        ps.setLineWidth(getInt(element, "lineWidth"));
        int alpha = -1;
        try {
            a = element.getAttribute("alpha");
            if (a != null) {
                alpha = a.getIntValue();
            }
        } catch (DataConversionException ex) {
            log.warn("invalid 'alpha' value (non integer)");
        }
        ps.setLineColor(getColor(element, "lineColor", alpha));
        ps.setFillColor(getColor(element, "fillColor", alpha));

        ps.makeShape();
        ps.rotate(getInt(element, "degrees"));

        a = element.getAttribute("hideOnSensor");
        boolean hide = false;
        if (a != null) {
            hide = a.getValue().equals("true");
        }
        int changeLevel = -1;
        try {
            changeLevel = getInt(element, "changeLevelOnSensor");
        } catch (Exception e) {
            log.error("failed to get changeLevel attribute ex= " + e);
        }
        try {
            Attribute attr = element.getAttribute("controlSensor");
            if (attr != null) {
                ps.setControlSensor(attr.getValue(), hide, changeLevel);
            }
        } catch (NullPointerException e) {
            log.error("incorrect information for controlSensor of PositionableShape");
        }
        ps.updateSize();
    }

    /* pre version 3.9.4 alpha was only used for fill color.
     * alpha == -1 indicates 3.9.4 or later
     */
    public Color getColor(Element element, String name, int alpha) {
        Element elem = element.getChild(name);
        if (elem == null) {
            return null;
        }
        try {
            int red = elem.getAttribute("red").getIntValue();
            int blue = elem.getAttribute("blue").getIntValue();
            int green = elem.getAttribute("green").getIntValue();
            if (alpha == -1) {
                alpha = elem.getAttribute("alpha").getIntValue();
                return new Color(red, green, blue, alpha);
            } else if (name.equals("lineColor")) {
                return new Color(red, green, blue);
            } else {
                return new Color(red, green, blue, alpha);
            }
        } catch (Exception e) {
            log.warn("failed to convert color attribute for " + name + " - " + e);
        }
        return null;
    }

    public int getInt(Element element, String name) {
        try {
            Attribute attr = element.getAttribute(name);
            if (attr != null) {
                int num = attr.getIntValue();
                return num;
            }
        } catch (Exception e) {
            log.error("failed to convert integer attribute for " + name + " - " + e);
        }
        return 0;
    }

    public float getFloat(Element element, String name) {
        try {
            Attribute attr = element.getAttribute(name);
            if (attr != null) {
                float num = attr.getFloatValue();
                return num;
            }
        } catch (Exception e) {
            log.error("failed to convert integer attribute for " + name + " - " + e);
        }
        return 0;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableShapeXml.class.getName());
}
