package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.SensorIcon;
//import jmri.util.NamedBeanHandle;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import java.awt.Color;
import java.util.List;

/**
 * Handle configuration for display.SensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.41 $
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
        element.setAttribute("sensor", p.getNameString());
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
        if (p.getText()==null) {
            return;
        }
        element.setAttribute("text", p.getText());
        element.setAttribute("size", ""+p.getFont().getSize());
        element.setAttribute("style", ""+p.getFont().getStyle());
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
        if (p.getText()!=null)
            element.setAttribute("text", p.getText());
        if (!p.isIcon()){
            Element textElement = new Element("activeText");
            if(p.getActiveText()!=null)
                textElement.setAttribute("text", p.getActiveText());
            if (!p.getTextActive().equals(Color.black)) {
                textElement.setAttribute("red", ""+p.getTextActive().getRed());
                textElement.setAttribute("green", ""+p.getTextActive().getGreen());
                textElement.setAttribute("blue", ""+p.getTextActive().getBlue());
            }
            if (p.getBackgroundActive()!=null) {
                textElement.setAttribute("redBack", ""+p.getBackgroundActive().getRed());
                textElement.setAttribute("greenBack", ""+p.getBackgroundActive().getGreen());
                textElement.setAttribute("blueBack", ""+p.getBackgroundActive().getBlue());
            }
            element.addContent(textElement);
            textElement = new Element("inactiveText");
            if(p.getInactiveText()!=null)
                textElement.setAttribute("text", p.getInactiveText());
            if(!p.getTextInActive().equals(Color.black)){
                textElement.setAttribute("red", ""+p.getTextInActive().getRed());
                textElement.setAttribute("green", ""+p.getTextInActive().getGreen());
                textElement.setAttribute("blue", ""+p.getTextInActive().getBlue());
            }
            if(p.getBackgroundInActive()!=null){
                textElement.setAttribute("redBack", ""+p.getBackgroundInActive().getRed());
                textElement.setAttribute("greenBack", ""+p.getBackgroundInActive().getGreen());
                textElement.setAttribute("blueBack", ""+p.getBackgroundInActive().getBlue());
            }
            element.addContent(textElement);
            
            textElement = new Element("unknownText");
            
            if(p.getUnknownText()!=null)
                textElement.setAttribute("text", p.getUnknownText());
            if(!p.getTextUnknown().equals(Color.black)){
                textElement.setAttribute("red", ""+p.getTextUnknown().getRed());
                textElement.setAttribute("green", ""+p.getTextUnknown().getGreen());
                textElement.setAttribute("blue", ""+p.getTextUnknown().getBlue());
            }   
            if(p.getBackgroundUnknown()!=null){
                textElement.setAttribute("redBack", ""+p.getBackgroundUnknown().getRed());
                textElement.setAttribute("greenBack", ""+p.getBackgroundUnknown().getGreen());
                textElement.setAttribute("blueBack", ""+p.getBackgroundUnknown().getBlue());
            }
            element.addContent(textElement);
            
            textElement = new Element("inconsistentText");
            if(p.getInconsistentText()!=null)
                textElement.setAttribute("text", p.getInconsistentText());
            if(!p.getTextInconsistent().equals(Color.black)){
                textElement.setAttribute("red", ""+p.getTextInconsistent().getRed());
                textElement.setAttribute("green", ""+p.getTextInconsistent().getGreen());
                textElement.setAttribute("blue", ""+p.getTextInconsistent().getBlue());
            }
            if(p.getBackgroundInconsistent()!=null){
                textElement.setAttribute("redBack", ""+p.getBackgroundInconsistent().getRed());
                textElement.setAttribute("greenBack", ""+p.getBackgroundInconsistent().getGreen());
                textElement.setAttribute("blueBack", ""+p.getBackgroundInconsistent().getBlue());
            }
            element.addContent(textElement);
        }
    }
    
    protected void storeIconInfo(SensorIcon p, Element element) {
        element.addContent(storeIcon("active", p.getActiveIcon()));
        element.addContent(storeIcon("inactive", p.getInactiveIcon()));
        element.addContent(storeIcon("unknown", p.getUnknownIcon()));
        element.addContent(storeIcon("inconsistent", p.getInconsistentIcon()));
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
        
        String name;
        Attribute attr = element.getAttribute("sensor"); 
        if (attr == null) {
            log.error("incorrect information for sensor; must use sensor name");
            return;
        } else {
            name = attr.getValue();
        }
        
        
        boolean icon=true;
        if (element.getAttribute("icon") != null){
            String yesno = element.getAttribute("icon").getValue();
            if ( (yesno!=null) && (!yesno.equals("")) ) {
                if (yesno.equals("yes")) icon=true;
                else if (yesno.equals("no")) icon=false;
            }
        }

        if (icon){
            int rotation = 0;
            try {
                rotation = element.getAttribute("rotate").getIntValue();
            } catch (org.jdom.DataConversionException e) {
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif", "resources/icons/smallschematics/tracksegments/circuit-error.gif"));
            if(pe!=null)
                pe.putLabel(l);
            else if (le!=null)
                le.putSensor(l);
            loadSensorIcon("active", rotation, l, element, name);
            loadSensorIcon("inactive", rotation, l, element, name);
            loadSensorIcon("unknown", rotation, l,element, name);
            loadSensorIcon("inconsistent", rotation, l,element, name);
            //loadIconInfo(l, element);
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
        
        l.setSensor(name);

        /*if(pe!=null)
            pe.putLabel(l);
        else if (le!=null)
            le.putSensor(l);*/

    }
    
    private void loadSensorIcon(String state, int rotation, SensorIcon l, Element element, String name){
        NamedIcon icon = loadIcon(l,state, element);
        if (icon==null){
            if (element.getAttribute(state) != null) {
                String iconName;
                iconName = element.getAttribute(state).getValue();
                icon = NamedIcon.getIconByName(iconName);
                icon.setRotation(rotation, l);
            }
            else log.warn("did not locate " + state + " icon file "+name);
        }
        if (icon!=null) {
            if(state.equals("active")) l.setActiveIcon(icon);
            else if (state.equals("inactive")) l.setInactiveIcon(icon);
            else if (state.equals("unknown")) l.setUnknownIcon(icon);
            else if (state.equals("inconsistent")) l.setInconsistentIcon(icon);
        }
    }
    
    /*void loadIconInfo(SensorIcon l, Element element){
        String name;
        NamedIcon icon = loadIcon( l,"active", element);
        if (icon!=null) { l.setActiveIcon(icon); }

        icon = loadIcon( l,"inactive", element);
        if (icon!=null) { l.setInactiveIcon(icon); }

        icon = loadIcon( l,"unknown", element);
        if (icon!=null) { l.setUnknownIcon(icon); }

        icon = loadIcon( l,"inconsistent", element);
        if (icon!=null) { l.setInconsistentIcon(icon); }
        
        //This deals with the old format of the files from the layout editor.
        if(icon==null){
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
        }

    
    }*/
    

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
        if (!l.isIcon()){
            loadSensorTextState("Active", l, element);
            loadSensorTextState("InActive", l, element);
            loadSensorTextState("Unknown", l, element);
            loadSensorTextState("Inconsistent", l, element);
            /*List<Element> textList = element.getChildren("activeText");
            if (log.isDebugEnabled()) log.debug("Found "+textList.size()+" activeText objects");
            if (textList.size()>0) {
                Element elem = textList.get(0);
                try {
                    name = elem.getAttribute("text").getValue();
                    l.setActiveText(name);
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = elem.getAttribute("red").getIntValue();
                    int blue = elem.getAttribute("blue").getIntValue();
                    int green = elem.getAttribute("green").getIntValue();
                    l.setTextActive(new Color(red, green, blue));
                } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = elem.getAttribute("redBack").getIntValue();
                    int blue = elem.getAttribute("blueBack").getIntValue();
                    int green = elem.getAttribute("greenBack").getIntValue();
                    l.setBackgroundActive(new Color(red, green, blue));
                } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
            
            } else {
                if (element.getAttribute("active")!=null){
                    name = element.getAttribute("active").getValue();
                    l.setActiveText(name);
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
                    int red = element.getAttribute("redActiveBack").getIntValue();
                    int blue = element.getAttribute("blueActiveBack").getIntValue();
                    int green = element.getAttribute("greenActiveBack").getIntValue();
                    l.setBackgroundActive(new Color(red, green, blue));
                } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
            }
            
            textList = element.getChildren("inactiveText");
            if (log.isDebugEnabled()) log.debug("Found "+textList.size()+" inactiveText objects");
            if (textList.size()>0) {
                Element elem = textList.get(0);
                try {
                    name = elem.getAttribute("text").getValue();
                    l.setActiveText(name);
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = elem.getAttribute("red").getIntValue();
                    int blue = elem.getAttribute("blue").getIntValue();
                    int green = elem.getAttribute("green").getIntValue();
                    l.setTextInActive(new Color(red, green, blue));
                    //l.setOpaque(true);
                 } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = elem.getAttribute("redBack").getIntValue();
                    int blue = elem.getAttribute("blueBack").getIntValue();
                    int green = elem.getAttribute("greenBack").getIntValue();
                    l.setBackgroundInActive(new Color(red, green, blue));
                    //l.setOpaque(true);
                 } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
        
            } else {
                try {
                    name = element.getAttribute("inactive").getValue();
                    l.setInactiveText(name);
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
                    int red = element.getAttribute("redInActiveBack").getIntValue();
                    int blue = element.getAttribute("blueInActiveBack").getIntValue();
                    int green = element.getAttribute("greenInActiveBack").getIntValue();
                    l.setBackgroundInActive(new Color(red, green, blue));
                    //l.setOpaque(true);
                 } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
            }
            textList = element.getChildren("unknownText");
            if (log.isDebugEnabled()) log.debug("Found "+textList.size()+" unknownText objects");
            if (textList.size()>0) {
                Element elem = textList.get(0);
                try {
                    name = elem.getAttribute("text").getValue();
                    l.setActiveText(name);
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = elem.getAttribute("red").getIntValue();
                    int blue = elem.getAttribute("blue").getIntValue();
                    int green = elem.getAttribute("green").getIntValue();
                    l.setTextUnknown(new Color(red, green, blue));
                    //l.setOpaque(true);
                 } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = elem.getAttribute("redBack").getIntValue();
                    int blue = elem.getAttribute("blueBack").getIntValue();
                    int green = elem.getAttribute("greenBack").getIntValue();
                    l.setBackgroundUnknown(new Color(red, green, blue));
                    //l.setOpaque(true);
                 } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
        
            } else {
                try {
                    name = element.getAttribute("unknown").getValue();
                    l.setUnknownText(name);
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = element.getAttribute("redUnknown").getIntValue();
                    int blue = element.getAttribute("blueUnknown").getIntValue();
                    int green = element.getAttribute("greenUnknown").getIntValue();
                    l.setTextUnknown(new Color(red, green, blue));
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
            }
            textList = element.getChildren("inconsistentText");
            if (log.isDebugEnabled()) log.debug("Found "+textList.size()+" inconsistentText objects");
            if (textList.size()>0) {
                Element elem = textList.get(0);
                try {
                    name = elem.getAttribute("text").getValue();
                    l.setInconsistentText(name);
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = elem.getAttribute("red").getIntValue();
                    int blue = elem.getAttribute("blue").getIntValue();
                    int green = elem.getAttribute("green").getIntValue();
                    l.setTextInconsistent(new Color(red, green, blue));
                    //l.setOpaque(true);
                 } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
                try {
                    int red = elem.getAttribute("redBack").getIntValue();
                    int blue = elem.getAttribute("blueBack").getIntValue();
                    int green = elem.getAttribute("greenBack").getIntValue();
                    l.setBackgroundInconsistent(new Color(red, green, blue));
                    //l.setOpaque(true);
                 } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
        
            } else {
                try {
                    name = element.getAttribute("inconsistent").getValue();
                    l.setInconsistentText(name);
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
                try {
                    int red = element.getAttribute("redInconsistentBack").getIntValue();
                    int blue = element.getAttribute("blueInconsistentBack").getIntValue();
                    int green = element.getAttribute("greenInconsistentBack").getIntValue();
                    l.setBackgroundInconsistent(new Color(red, green, blue));
                 } catch ( org.jdom.DataConversionException e) {
                    log.warn("Could not parse color attributes!");
                } catch ( NullPointerException e) {  // considered normal if the attributes are not present
                }
            }*/
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
        } else {
            if (element.getAttribute("text")!=null){
                name = element.getAttribute("text").getValue();
                l.setText(name);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadSensorTextState(String state, SensorIcon l, Element element){
        String name = null;
        Color clrText=null;
        Color clrBackground=null;
        List<Element> textList = element.getChildren(state.toLowerCase()+"Text");
        if (log.isDebugEnabled()) log.debug("Found "+textList.size()+" "+state+"Text objects");
        if (textList.size()>0) {
            Element elem = textList.get(0);
            try {
                name = elem.getAttribute("text").getValue();
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = elem.getAttribute("red").getIntValue();
                int blue = elem.getAttribute("blue").getIntValue();
                int green = elem.getAttribute("green").getIntValue();
                clrText = new Color(red, green, blue);
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = elem.getAttribute("redBack").getIntValue();
                int blue = elem.getAttribute("blueBack").getIntValue();
                int green = elem.getAttribute("greenBack").getIntValue();
                clrBackground = new Color(red, green, blue);
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
        
        } else {
            if (element.getAttribute(state.toLowerCase())!=null){
                name = element.getAttribute(state.toLowerCase()).getValue();
            }
            try {
                int red = element.getAttribute("red"+state).getIntValue();
                int blue = element.getAttribute("blue"+state).getIntValue();
                int green = element.getAttribute("green"+state).getIntValue();
                clrText = new Color(red, green, blue);
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = element.getAttribute("red"+state+"Back").getIntValue();
                int blue = element.getAttribute("blue"+state+"Back").getIntValue();
                int green = element.getAttribute("green"+state+"Back").getIntValue();
                clrBackground = new Color(red, green, blue);
            } catch ( org.jdom.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch ( NullPointerException e) {  // considered normal if the attributes are not present
            }
        }
        if (state.equals("Active")){
            if (name!=null) l.setActiveText(name);
            if (clrText!=null) l.setTextActive(clrText);
            if (clrBackground!=null) l.setBackgroundActive(clrBackground);
        } else if (state.equals("InActive")){
            if (name!=null) l.setInactiveText(name);
            if (clrText!=null) l.setTextInActive(clrText);
            if (clrBackground!=null) l.setBackgroundInActive(clrBackground);
        } else if (state.equals("Unknown")){
            if (name!=null) l.setUnknownText(name);
            if (clrText!=null) l.setTextUnknown(clrText);
            if (clrBackground!=null) l.setBackgroundUnknown(clrBackground);
        } else if (state.equals("Inconsistent")){
            if (name!=null) l.setInconsistentText(name);
            if (clrText!=null) l.setTextInconsistent(clrText);
            if (clrBackground!=null) l.setBackgroundInconsistent(clrBackground);
        }
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorIconXml.class.getName());

}