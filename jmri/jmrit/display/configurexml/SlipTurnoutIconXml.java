package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SlipTurnoutIcon;
import org.jdom.Attribute;
import java.util.List;
import org.jdom.Element;

/**
 * Handle configuration for display.TurnoutIcon objects.
 *
 * Based upon the TurnoutIconXml by Bob Jacobsen
 * @author Kevin Dickerson Copyright: Copyright (c) 2010
 * @version $Revision: 1.2 $
 */
public class SlipTurnoutIconXml extends PositionableLabelXml {

    public SlipTurnoutIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * TurnoutIcon
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        SlipTurnoutIcon p = (SlipTurnoutIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("slipturnouticon");
        element.setAttribute("turnoutEast", p.getNamedTurnout(true).getName());
        element.setAttribute("turnoutWest", p.getNamedTurnout(false).getName());
        storeCommonAttributes(p, element);

        // include contents
        element.setAttribute("tristate", p.getTristate()?"true":"false");
        //element.setAttribute("turnoutType", p.getTurnoutType()?"double":"single");
        
        // new style
        element.addContent(storeIcon("lowerWestToUpperEast", p.getLowerWestToUpperEastIcon(), p.getLWUEText()));
        element.addContent(storeIcon("upperWestToLowerEast", p.getUpperWestToLowerEastIcon(), p.getUWLEText()));
        switch(p.getTurnoutType()){
            case 0x00 : 
                element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(),p.getLWLEText()));
                element.addContent(storeIcon("upperWestToUpperEast", p.getUpperWestToUpperEastIcon(),p.getUWUEText()));
                element.setAttribute("turnoutType", "doubleSlip");
                break;
            case 0x02:
                element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(),p.getLWLEText()));
                element.setAttribute("turnoutType", "singleSlip");
                element.setAttribute("singleSlipRoute", p.getSingleSlipRoute()?"upperWestToUpperEast":"lowerWestToLowerEast");
                break;
            case 0x04:
                element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(),p.getLWLEText()));
                element.setAttribute("turnoutType", "threeWay");
                element.setAttribute("firstTurnoutExit", p.getSingleSlipRoute()?"upper":"lower");
                //TBC
                break;
        }
        
        
        /*if (p.getTurnoutType()){
            element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(),p.getLWLEText()));
            element.addContent(storeIcon("upperWestToUpperEast", p.getUpperWestToUpperEastIcon(),p.getUWUEText()));
        } else {
            element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(),p.getLWLEText()));
            element.setAttribute("singleSlipRoute", p.getSingleSlipRoute()?"upperWestToUpperEast":"lowerWestToLowerEast");
        }*/

        element.addContent(super.storeIcon("unknown", p.getUnknownIcon()));
        element.addContent(super.storeIcon("inconsistent", p.getInconsistentIcon()));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.SlipTurnoutIconXml");

        return element;
    }
    
    Element storeIcon(String elemName, NamedIcon icon, String text){
        Element element = super.storeIcon(elemName, icon);
        element.addContent(new Element("text").addContent(text));
        return element;
    }
    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }
    
    

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  Editor as an Object
     */
    @SuppressWarnings("null")
	public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor)o;

        SlipTurnoutIcon l = new SlipTurnoutIcon(p);
        int rotation = 0;
        try {
            Attribute a = element.getAttribute("rotate");
            rotation = a.getIntValue();
        } catch (org.jdom.DataConversionException e) {
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        
        String nameEast;
        try {
            nameEast=element.getAttribute("turnoutEast").getValue();
        } catch ( NullPointerException e) { 
            log.error("incorrect information for turnout; must use turnout name");
            return;
        }
        String nameWest;
        try {
            nameWest=element.getAttribute("turnoutWest").getValue();
        } catch ( NullPointerException e) { 
            log.error("incorrect information for turnout; must use turnout name");
            return;
        }
        
        Attribute a = element.getAttribute("turnoutType");
        if (a!=null) {
            if (a.getValue().equals("doubleSlip")) {
                l.setTurnoutType(l.DOUBLESLIP);
            } else if (a.getValue().equals("singleSlip")) {
                l.setTurnoutType(l.SINGLESLIP);
                a = element.getAttribute("singleSlipRoute");
                if ( (a==null) || ((a!=null) && a.getValue().equals("upperWestToUpperEast")))
                    l.setSingleSlipRoute(true);
                else 
                    l.setSingleSlipRoute(false);
            } else if (a.getValue().equals("threeWay")) {
                l.setTurnoutType(l.THREEWAY);
                a = element.getAttribute("firstTurnoutExit");
                if ( (a==null) || ((a!=null) && a.getValue().equals("lower")))
                    l.setSingleSlipRoute(false);
                else
                    l.setSingleSlipRoute(true);
            }
        }
        
        loadTurnoutIcon("lowerWestToUpperEast", rotation, l, element);
        loadTurnoutIcon("upperWestToLowerEast", rotation, l, element);
        switch(l.getTurnoutType()){
            case 0x00 : 
                loadTurnoutIcon("lowerWestToLowerEast", rotation, l, element);
                loadTurnoutIcon("upperWestToUpperEast", rotation, l, element);
                break;
            case 0x02:
                loadTurnoutIcon("lowerWestToLowerEast", rotation, l, element);
                break;
            case 0x04:
                //TBC
                loadTurnoutIcon("lowerWestToLowerEast", rotation, l, element);
                break;
        }
        /*if(l.getTurnoutType()==0x00){
            loadTurnoutIcon("lowerWestToLowerEast", rotation, l, element);
            loadTurnoutIcon("upperWestToUpperEast", rotation, l, element);
        } else {
            loadTurnoutIcon("lowerWestToLowerEast", rotation, l, element);
        }*/
        loadTurnoutIcon("unknown", rotation, l, element);
        loadTurnoutIcon("inconsistent", rotation, l, element);
        
        a = element.getAttribute("tristate");
        if ( (a==null) || ((a!=null) && a.getValue().equals("true")))
            l.setTristate(true);
        else
            l.setTristate(false);
            
        l.setTurnout(nameEast, true);
        l.setTurnout(nameWest, false);
        
        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }
    
    @SuppressWarnings("unchecked")
    private void loadTurnoutIcon(String state, int rotation, SlipTurnoutIcon l, Element element){
        NamedIcon icon = loadIcon(l, state, element);
        String textValue = null;

        if (icon!=null){
            if(state.equals("lowerWestToUpperEast")) l.setLowerWestToUpperEastIcon(icon);
            else if (state.equals("upperWestToLowerEast")) l.setUpperWestToLowerEastIcon(icon);
            else if (state.equals("lowerWestToLowerEast")) l.setLowerWestToLowerEastIcon(icon);
            else if (state.equals("upperWestToUpperEast")) l.setUpperWestToUpperEastIcon(icon);
            else if (state.equals("unknown")) l.setUnknownIcon(icon);
            else if (state.equals("inconsistent")) l.setInconsistentIcon(icon);
        }
        Element elem = element.getChild(state);
        if (elem!=null){
            Element e = elem.getChild("text");
            if (e!=null)
                textValue = e.getText();
        }
        if (textValue!=null){
            if(state.equals("lowerWestToUpperEast")) l.setLWUEText(textValue);
            else if (state.equals("upperWestToLowerEast")) l.setUWLEText(textValue);
            else if (state.equals("lowerWestToLowerEast")) l.setLWLEText(textValue);
            else if (state.equals("upperWestToUpperEast")) l.setUWUEText(textValue);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SlipTurnoutIconXml.class.getName());
}