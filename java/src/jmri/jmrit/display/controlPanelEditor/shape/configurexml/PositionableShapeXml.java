package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import java.awt.Color;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.configurexml.AbstractXmlAdapter;
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
 * @author Pete Cressman Copyright (c) 2012
 */
public abstract class PositionableShapeXml extends AbstractXmlAdapter {

    public PositionableShapeXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionableShape
     *
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    @Override
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
     * Default implementation for storing the common contents.
     *
     * @param p       the shape to store
     * @param element Element in which contents are stored
     */
    public void storeCommonAttributes(PositionableShape p, Element element) {
        element.setAttribute("x", "" + p.getX());
        element.setAttribute("y", "" + p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.setAttribute("forcecontroloff", !p.isControlling() ? "true" : "false");
        element.setAttribute("hidden", p.isHidden() ? "yes" : "no");
        element.setAttribute("positionable", p.isPositionable() ? "true" : "false");
        element.setAttribute("showtooltip", p.showToolTip() ? "true" : "false");
        element.setAttribute("editable", p.isEditable() ? "true" : "false");
        ToolTip tip = p.getToolTip();
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

    public void loadCommonAttributes(PositionableShape ps, int defaultLevel, Element element) {
        int x = getInt(element, "x");
        int y = getInt(element, "y");
        ps.setLocation(x, y);

        ps.setDisplayLevel(getInt(element, "level"));

        try {
            boolean value = element.getAttribute("hidden").getBooleanValue();
            ps.setHidden(value);
            ps.setVisible(!value);
        } catch (DataConversionException e1) {
            log.warn("unable to convert positionable shape hidden attribute");
        }

        try {
            ps.setPositionable(element.getAttribute("positionable").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert positionable shape positionable attribute");
        }

        try {
            ps.setShowToolTip(element.getAttribute("showtooltip").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert positionable shape showtooltip attribute");
        }

        try {
            ps.setEditable(element.getAttribute("editable").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert positionable shape editable attribute");
        }

        Element elem = element.getChild("toolTip");
        if (elem != null) {
            ToolTip tip = ps.getToolTip();
            if (tip != null) {
                tip.setText(elem.getText());
            }
        }
        ps.setLineWidth(getInt(element, "lineWidth"));

        int alpha = -1;
        Attribute a = element.getAttribute("alpha");
        try {
            if (a != null) {
                alpha = a.getIntValue();
            }
        } catch (DataConversionException ex) {
            log.warn("invalid 'alpha' value (non integer)");
        }
        ps.setLineColor(getColor(element, "lineColor", alpha));
        ps.setFillColor(getColor(element, "fillColor", alpha));

        ps.rotate(getInt(element, "degrees"));

        boolean hide = false;
        try {
            hide = element.getAttribute("hideOnSensor").getBooleanValue();
        } catch (DataConversionException e1) {
            log.warn("unable to convert positionable shape hideOnSensor attribute");
        }
        ps.setHide(hide);

        int changeLevel = 2;
        try {
            changeLevel = getInt(element, "changeLevelOnSensor");
        } catch (Exception e) {
            log.error("failed to get changeLevel attribute ex= {}", e.getMessage());
        }
        ps.setChangeLevel(changeLevel);

        try {
            a = element.getAttribute("controlSensor");
            if (a != null) {
                ps.setControlSensor(a.getValue());
                ps.setListener();
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
        } catch (DataConversionException e) {
            log.warn("failed to convert color attribute for {} - {}", name, e);
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
        } catch (DataConversionException e) {
            log.error("failed to convert integer attribute for {} - {}", name, e);
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
        } catch (DataConversionException e) {
            log.error("failed to convert integer attribute for {} - {}", name, e);
        }
        return 0;
    }

    private final static Logger log = LoggerFactory.getLogger(PositionableShapeXml.class);
}
