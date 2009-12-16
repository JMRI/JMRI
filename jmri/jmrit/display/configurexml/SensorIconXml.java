package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.SensorIcon;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import java.awt.Color;

/**
 * Handle configuration for display.SensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.37 $
 */
public class SensorIconXml extends PositionableLabelXml {

    public SensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SensorIcon
     * @param o Object to store, of type SensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        SensorIcon p = (SensorIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("sensoricon");
        element.setAttribute("sensor", p.getSensor().getSystemName());
        storeCommonAttributes(p, element);
        element.setAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");
        element.setAttribute("momentary", p.getMomentary()?"true":"false");
        if (p.isIcon()){
            element.setAttribute("icon", "yes");
            storeIconInfo(p, element);
        } else
            element.setAttribute("icon", "no");
        // An icon can have text with it.
        if (p.isText())
            storeTextInfo(p, element);
        element.setAttribute("class", "jmri.jmrit.display.configurexml.SensorIconXml");
        return element;
    }
    
    protected void storeTextInfo(SensorIcon p, Element element) {
            element.setAttribute("size", ""+p.getFont().getSize());
            element.setAttribute("style", ""+p.getFont().getStyle());
            if (p.getText()!=null)
                element.setAttribute("text", p.getText());
            if (!p.isIcon()){
                if(p.getActiveText()!=null)
                    element.setAttribute("active", p.getActiveText());
                if(p.getInactiveText()!=null)
                    element.setAttribute("inactive", p.getInactiveText());
                if(p.getUnknownText()!=null)
                    element.setAttribute("unknown", p.getUnknownText());
                if(p.getInconsistentText()!=null)
                    element.setAttribute("inconsistent", p.getInconsistentText());
            }
            if (p.getBackgroundActive()!=null) {
                element.setAttribute("redActiveBack", ""+p.getBackgroundActive().getRed());
                element.setAttribute("greenActiveBack", ""+p.getBackgroundActive().getGreen());
                element.setAttribute("blueActiveBack", ""+p.getBackgroundActive().getBlue());
            }
            //if(p.getBackgroundInActive().equals(new Color(238, 238, 238))){
            if(p.getBackgroundInActive()!=null){
                element.setAttribute("redInActiveBack", ""+p.getBackgroundInActive().getRed());
                element.setAttribute("greenInActiveBack", ""+p.getBackgroundInActive().getGreen());
                element.setAttribute("blueInActiveBack", ""+p.getBackgroundInActive().getBlue());
            }
            if(p.getBackgroundUnknown()!=null){
                element.setAttribute("redUnknownBack", ""+p.getBackgroundUnknown().getRed());
                element.setAttribute("greenUnknownBack", ""+p.getBackgroundUnknown().getGreen());
                element.setAttribute("blueUnknownBack", ""+p.getBackgroundUnknown().getBlue());
            }

            if(p.getBackgroundUnknown()!=null){
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
    
    protected void storeIconInfo(SensorIcon p, Element element) {
        element.setAttribute("active", p.getActiveIcon().getURL());
        element.setAttribute("inactive", p.getInactiveIcon().getURL());
        element.setAttribute("unknown", p.getUnknownIcon().getURL());
        element.setAttribute("inconsistent", p.getInconsistentIcon().getURL());
        element.setAttribute("rotate", String.valueOf(p.getActiveIcon().getRotation()));
        if(p.getLayoutPanel()==null){
            element.addContent(storeIcon("active", p.getActiveIcon()));
            element.addContent(storeIcon("inactive", p.getInactiveIcon()));
            element.addContent(storeIcon("unknown", p.getUnknownIcon()));
            element.addContent(storeIcon("inconsistent", p.getInconsistentIcon()));
        }
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
        // create the objects
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

        SensorIcon l;
        boolean icon=true;
        if (element.getAttribute("icon") != null){
            String yesno = element.getAttribute("icon").getValue();
            if ( (yesno!=null) && (!yesno.equals("")) ) {
                if (yesno.equals("yes")) icon=true;
                else if (yesno.equals("no")) icon=false;
            }
        }

        if (icon){
            l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
            if(pe!=null)
                pe.putLabel(l);
            else if (le!=null)
                le.putSensor(l);
            loadIconInfo(l, element);
        } else {
            l = new SensorIcon(new String("  "));
            if(pe!=null)
                pe.putLabel(l);
            else if (le!=null)
                le.putSensor(l);

        }
/*        if (element.getAttribute("icon")!=null){
            if (element.getAttribute("icon").getValue().equals("no"))
                l = new SensorIcon(new String("  "));
            else l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
        } else
            l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));*/

        if (pe!=null){
            //l.setPanel(pe);
            loadCommonAttributes(l, PanelEditor.SENSORS.intValue(), element);
        }else if (le!=null){
            //l.setPanel(le);
            loadCommonAttributes(l, LayoutEditor.SENSORS.intValue(), element);
        }
        
        loadTextInfo(l, element);
        
        Attribute a = element.getAttribute("momentary");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setMomentary(true);
        else
            l.setMomentary(false);
        
        l.setSensor(element.getAttribute("sensor").getValue());
        /*if(pe!=null)
            pe.putLabel(l);
        else if (le!=null)
            le.putSensor(l);*/
    }
    
    void loadIconInfo(SensorIcon l, Element element){
        String name;
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
            Attribute a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = element.getAttribute("rotate").getIntValue();
                active.setRotation(rotation, l);
                inactive.setRotation(rotation, l);
                inconsistent.setRotation(rotation, l);
                unknown.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}
        
        NamedIcon icon = loadIcon( l,"active", element);
        if (icon!=null) { l.setActiveIcon(icon); }

        icon = loadIcon( l,"inactive", element);
        if (icon!=null) { l.setInactiveIcon(icon); }

        icon = loadIcon( l,"unknown", element);
        if (icon!=null) { l.setUnknownIcon(icon); }

        icon = loadIcon( l,"inconsistent", element);
        if (icon!=null) { l.setInconsistentIcon(icon); }
    
    }
    
    void loadTextInfo(SensorIcon l, Element element){
        Attribute a = element.getAttribute("size");
        try {
            if (a!=null){ 
                l.setFontSize(a.getFloatValue());
            }
        } catch (DataConversionException ex) {
            log.warn("invalid size attribute value");
        }

        a = element.getAttribute("style");
        try {
            if (a!=null){
                int style = a.getIntValue();
                int drop = 0;
                switch (style){
                    case 0: drop = 1; //0 Normal
                            break;
                    case 2: drop = 1; //italic
                            break;
                }
                l.setFontStyle(style, drop);
            }
        } catch (DataConversionException ex) {
            log.warn("invalid style attribute value");
        }
        String name;
        if (element.getAttribute("text")!=null){
            name = element.getAttribute("text").getValue();
            l.setText(name);
        }
        if (!l.isIcon()){
            if (element.getAttribute("active")!=null){
                name = element.getAttribute("active").getValue();
                l.setActiveText(name);
            }
            
            if (element.getAttribute("inactive")!=null){
                name = element.getAttribute("inactive").getValue();
                l.setInactiveText(name);
            }
            
            if (element.getAttribute("unknown")!=null){
                name = element.getAttribute("unknown").getValue();
                l.setUnknownText(name);
            }
            
            if (element.getAttribute("inconsistent")!=null){
                name = element.getAttribute("inconsistent").getValue();
                l.setInconsistentText(name);
            }
        }
        

        /*a = element.getAttribute("style");
        try {
            if (a!=null)
                l.setFontStyle(a.getIntValue(), 0);  // label is created plain, so don't need to drop
        } catch (DataConversionException ex) {
            log.warn("invalid style attribute value");
        }*/
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
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        try {
            fixedHeight=element.getAttribute("fixedHeight").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        if (!(fixedWidth==0 && fixedHeight==0))
            l.setFixedSize(fixedWidth, fixedHeight);
        try {
            l.setBorderSize(element.getAttribute("borderSize").getIntValue());
            int red = element.getAttribute("redBorder").getIntValue();
            int blue = element.getAttribute("blueBorder").getIntValue();
            int green = element.getAttribute("greenBorder").getIntValue();
            l.setBorderColor(new Color(red, green, blue));
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
    }
    

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorIconXml.class.getName());

}