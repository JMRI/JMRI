package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.LayoutSensorIcon;
import org.jdom.DataConversionException;
import org.jdom.Attribute;
import org.jdom.Element;
import java.awt.Color;

/**
 * This module handles configuration for display.LayoutSensorIcon objects for a LayoutEditor.
 *   This routine is almost identical to SensorIconXml.java, written by Bob Jacobsen.  
 *   Differences are related to the hard interdependence between SensorIconXml.java and 
 *   PanelEditor.java, which made it impossible to use SensorIconXml.java directly with 
 *   LayoutEditor. Rectifying these differences is especially important when storing and
 *   loading a saved panel.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision: 1.6 $
 */
public class LayoutSensorIconXml implements XmlAdapter {

    public LayoutSensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutSensorIcon
     * @param o Object to store, of type LayoutSensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LayoutSensorIcon p = (LayoutSensorIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("sensoricon");

        // include contents
        element.setAttribute("sensor", p.getSensor().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        if (p.isIcon()){
            element.setAttribute("icon", "yes");
            element.setAttribute("active", p.getActiveIcon().getName());
            element.setAttribute("inactive", p.getInactiveIcon().getName());
            element.setAttribute("unknown", p.getUnknownIcon().getName());
            element.setAttribute("inconsistent", p.getInconsistentIcon().getName());
            element.setAttribute("rotate", String.valueOf(p.getActiveIcon().getRotation()));
        } else {
            element.setAttribute("icon", "no");
            element.setAttribute("size", ""+p.getFont().getSize());
            element.setAttribute("style", ""+p.getFont().getStyle());
            if (!p.getBackgroundActive().equals(new Color(238, 238, 238))) {
                element.setAttribute("redActiveBack", ""+p.getBackgroundActive().getRed());
                element.setAttribute("greenActiveBack", ""+p.getBackgroundActive().getGreen());
                element.setAttribute("blueActiveBack", ""+p.getBackgroundActive().getBlue());
            }
            if(!p.getBackgroundInActive().equals(new Color(238, 238, 238))){
                element.setAttribute("redInActiveBack", ""+p.getBackgroundInActive().getRed());
                element.setAttribute("greenInActiveBack", ""+p.getBackgroundInActive().getGreen());
                element.setAttribute("blueInActiveBack", ""+p.getBackgroundInActive().getBlue());
            }
            if(!p.getBackgroundUnknown().equals(new Color(238, 238, 238))){
                element.setAttribute("redUnknownBack", ""+p.getBackgroundUnknown().getRed());
                element.setAttribute("greenUnknownBack", ""+p.getBackgroundUnknown().getGreen());
                element.setAttribute("blueUnknownBack", ""+p.getBackgroundUnknown().getBlue());
            }

            if(!p.getBackgroundUnknown().equals(new Color(238, 238, 238))){
                element.setAttribute("redInconsistentBack", ""+p.getBackgroundInconsistent().getRed());
                element.setAttribute("greenInconsistentBack", ""+p.getBackgroundInconsistent().getGreen());
                element.setAttribute("blueInconsistentBack", ""+p.getBackgroundInconsistent().getBlue());
            }
            if (!p.getTextActive().equals(Color.black)) {
                element.setAttribute("redActive", ""+p.getTextActive().getRed());
                element.setAttribute("greenActive", ""+p.getTextActive().getGreen());
                element.setAttribute("blueActive", ""+p.getTextActive().getBlue());
            }
            if(!p.getTextInActive().equals(Color.black)){
                element.setAttribute("redInActive", ""+p.getTextInActive().getRed());
                element.setAttribute("greenInActive", ""+p.getTextInActive().getGreen());
                element.setAttribute("blueInActive", ""+p.getTextInActive().getBlue());
            }
            if(!p.getTextUnknown().equals(Color.black)){
                element.setAttribute("redUnknown", ""+p.getTextUnknown().getRed());
                element.setAttribute("greenUnknown", ""+p.getTextUnknown().getGreen());
                element.setAttribute("blueUnknown", ""+p.getTextUnknown().getBlue());
            }

            if(!p.getTextUnknown().equals(Color.black)){
                element.setAttribute("redInconsistent", ""+p.getTextInconsistent().getRed());
                element.setAttribute("greenInconsistent", ""+p.getTextInconsistent().getGreen());
                element.setAttribute("blueInconsistent", ""+p.getTextInconsistent().getBlue());
            }
            
            
            element.setAttribute("active", p.getActiveText());
            element.setAttribute("inactive", p.getInactiveText());
            element.setAttribute("unknown", p.getUnknownText());
            element.setAttribute("inconsistent", p.getInconsistentText());
            if (p.getMargin()!=0)
                element.setAttribute("margin", ""+p.getMargin());
                    if (p.getBorderSize()!=0){
                element.setAttribute("borderSize", ""+p.getBorderSize());
                element.setAttribute("redBorder", ""+p.getBorderColor().getRed());
                element.setAttribute("greenBorder", ""+p.getBorderColor().getGreen());
                element.setAttribute("blueBorder", ""+p.getBorderColor().getBlue());
            } 
            if (p.getFixedWidth()!=0)
                element.setAttribute("fixedWidth", ""+p.getFixedWidth());
            if (p.getFixedHeight()!=0)
                element.setAttribute("fixedHeight", ""+p.getFixedHeight());
                            
        }
            
        element.setAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");
        element.setAttribute("momentary", p.getMomentary()?"true":"false");

        element.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutSensorIconXml");

        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
        String name;
        LayoutSensorIcon l;
        Attribute a;
        
        if (element.getAttribute("icon")!=null){
            if (element.getAttribute("icon").getValue().equals("no"))
                l = new LayoutSensorIcon(new String("  "));
            else l = new LayoutSensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        } else
            l = new LayoutSensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
            //Stuff in here for a label option.
        
        if (l.isIcon()){
            NamedIcon active;
            name = element.getAttribute("active").getValue();
            l.setActiveIcon(active = NamedIcon.getIconByName(name));
            NamedIcon inactive;
            name = element.getAttribute("inactive").getValue();
            l.setInactiveIcon(inactive = NamedIcon.getIconByName(name));

            NamedIcon unknown;
            name = element.getAttribute("unknown").getValue();
            l.setUnknownIcon(unknown = NamedIcon.getIconByName(name));

            NamedIcon inconsistent;
            name = element.getAttribute("inconsistent").getValue();
            l.setInconsistentIcon(inconsistent = NamedIcon.getIconByName(name));

            try {
                a = element.getAttribute("rotate");
                if (a!=null) {
                    int rotation = element.getAttribute("rotate").getIntValue();
                    active.setRotation(rotation, l);
                    inactive.setRotation(rotation, l);
                    inconsistent.setRotation(rotation, l);
                    unknown.setRotation(rotation, l);
                }
            } catch (org.jdom.DataConversionException e) {}

        } else {
            String active;
            if (element.getAttribute("inactive")!=null){
                name = element.getAttribute("active").getValue();
                l.setActiveText(name);
            }
            
            String inactive;
            if (element.getAttribute("inactive")!=null){
                name = element.getAttribute("inactive").getValue();
                l.setInactiveText(name);
            }
            
            String unknown;
            if (element.getAttribute("inactive")!=null){
                name = element.getAttribute("unknown").getValue();
                l.setUnknownText(name);
            }
            
            String inconsistent;
            if (element.getAttribute("inactive")!=null){
                name = element.getAttribute("inconsistent").getValue();
                l.setInconsistentText(name);
            }
            a = element.getAttribute("size");
            try {
                if (a!=null) l.setFontSize(a.getFloatValue());
            } catch (DataConversionException ex) {
                log.warn("invalid size attribute value");
            }
            a = element.getAttribute("style");
            try {
                if (a!=null) l.setFontStyle(a.getIntValue(), 0);  // label is created plain, so don't need to drop
            } catch (DataConversionException ex) {
                log.warn("invalid style attribute value");
            }
            try {
                int red = element.getAttribute("redActiveBack").getIntValue();
                int blue = element.getAttribute("blueActiveBack").getIntValue();
                int green = element.getAttribute("greenActiveBack").getIntValue();
                l.setBackgroundActive(new Color(red, green, blue));
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            
            try {
                int red = element.getAttribute("redInActiveBack").getIntValue();
                int blue = element.getAttribute("blueInActiveBack").getIntValue();
                int green = element.getAttribute("greenInActiveBack").getIntValue();
                l.setBackgroundInActive(new Color(red, green, blue));
                //l.setOpaque(true);
             } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            
            try {
                int red = element.getAttribute("redUnknownBack").getIntValue();
                int blue = element.getAttribute("blueUnknownBack").getIntValue();
                int green = element.getAttribute("greenUnknownBack").getIntValue();
                l.setBackgroundUnknown(new Color(red, green, blue));
               // l.setOpaque(true);
             } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = element.getAttribute("redInconsistentBack").getIntValue();
                int blue = element.getAttribute("blueInconsistentBack").getIntValue();
                int green = element.getAttribute("greenInconsistentBack").getIntValue();
                l.setBackgroundInconsistent(new Color(red, green, blue));
             } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = element.getAttribute("redActive").getIntValue();
                int blue = element.getAttribute("blueActive").getIntValue();
                int green = element.getAttribute("greenActive").getIntValue();
                l.setTextActive(new Color(red, green, blue));
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            
            try {
                int red = element.getAttribute("redInActive").getIntValue();
                int blue = element.getAttribute("blueInActive").getIntValue();
                int green = element.getAttribute("greenInActive").getIntValue();
                l.setTextInActive(new Color(red, green, blue));
                //l.setOpaque(true);
             } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            
            try {
                int red = element.getAttribute("redUnknown").getIntValue();
                int blue = element.getAttribute("blueUnknown").getIntValue();
                int green = element.getAttribute("greenUnknown").getIntValue();
                l.setTextUnknown(new Color(red, green, blue));
               // l.setOpaque(true);
             } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = element.getAttribute("redInconsistent").getIntValue();
                int blue = element.getAttribute("blueInconsistent").getIntValue();
                int green = element.getAttribute("greenInconsistent").getIntValue();
                l.setTextInconsistent(new Color(red, green, blue));
             } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            int margin=0;
            try {
                margin=element.getAttribute("margin").getIntValue();
                l.setMargin(margin);
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            int fixedWidth=0;
            int fixedHeight=0;
            try {
                fixedWidth=element.getAttribute("fixedWidth").getIntValue();
                fixedHeight=element.getAttribute("fixedHeight").getIntValue();
                l.setFixedSize(fixedWidth, fixedHeight);
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                l.setBorderSize(element.getAttribute("borderSize").getIntValue());
                int red = element.getAttribute("redBorder").getIntValue();
                int blue = element.getAttribute("blueBorder").getIntValue();
                int green = element.getAttribute("greenBorder").getIntValue();
                l.setBorderColor(new Color(red, green, blue));
                //l.setBorder(new LineBorder(l.getBorderColor(), l.getBorderSize()));
                
                //l.setBorderWidth(new Color(red, green, blue));
                //l.setOpaque(true);
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse level attribute!");
            } catch ( NullPointerException e) {  // considered normal if the attribute not present
            }
            if ((fixedWidth==0) && (margin==0))
                l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
            else if ((fixedWidth==0) && (margin!=0))
                l.setSize(l.getPreferredSize().width+(margin*2), l.getPreferredSize().height+(margin*2));
            else
                l.setSize(fixedWidth, fixedHeight);
        }
        a = element.getAttribute("forcecontroloff");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setForceControlOff(true);
        else
            l.setForceControlOff(false);
            
        a = element.getAttribute("momentary");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setMomentary(true);
        else
            l.setMomentary(false);
            
        l.setSensor(element.getAttribute("sensor").getValue());

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
        int level = LayoutEditor.SENSORS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        p.putSensor(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutSensorIconXml.class.getName());

}