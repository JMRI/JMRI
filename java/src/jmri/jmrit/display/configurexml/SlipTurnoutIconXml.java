package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SlipTurnoutIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.TurnoutIcon objects.
 *
 * Based upon the TurnoutIconXml by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2010
 */
public class SlipTurnoutIconXml extends PositionableLabelXml {

    public SlipTurnoutIconXml() {
    }

    /**
     * Default implementation for storing the contents of a TurnoutIcon
     *
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        SlipTurnoutIcon p = (SlipTurnoutIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("slipturnouticon");

        element.addContent(new Element("turnoutEast").addContent(p.getNamedTurnout(SlipTurnoutIcon.EAST).getName()));
        element.addContent(new Element("turnoutWest").addContent(p.getNamedTurnout(SlipTurnoutIcon.WEST).getName()));

        switch (p.getTurnoutType()) {
            case SlipTurnoutIcon.DOUBLESLIP:
                element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(), p.getLWLEText()));
                element.addContent(storeIcon("upperWestToUpperEast", p.getUpperWestToUpperEastIcon(), p.getUWUEText()));
                element.setAttribute("turnoutType", "doubleSlip");
                break;
            case SlipTurnoutIcon.SINGLESLIP:
                element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(), p.getLWLEText()));
                element.setAttribute("turnoutType", "singleSlip");
                element.setAttribute("singleSlipRoute", p.getSingleSlipRoute() ? "upperWestToUpperEast" : "lowerWestToLowerEast");
                break;
            case SlipTurnoutIcon.THREEWAY:
                element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(), p.getLWLEText()));
                element.setAttribute("turnoutType", "threeWay");
                element.setAttribute("firstTurnoutExit", p.getSingleSlipRoute() ? "upper" : "lower");
                break;
            case SlipTurnoutIcon.SCISSOR:
                if (!p.getSingleSlipRoute()) {
                    element.addContent(new Element("turnoutLowerEast").addContent(p.getNamedTurnout(SlipTurnoutIcon.LOWEREAST).getName()));
                    element.addContent(new Element("turnoutLowerWest").addContent(p.getNamedTurnout(SlipTurnoutIcon.LOWERWEST).getName()));
                }
                element.addContent(storeIcon("lowerWestToLowerEast", p.getLowerWestToLowerEastIcon(), p.getLWLEText()));
                element.setAttribute("turnoutType", "scissor");
                break;
            default:
                log.warn("Unhandled turnout type: {}", p.getTurnoutType());
                break;
        }

        storeCommonAttributes(p, element);

        // include contents
        element.setAttribute("tristate", p.getTristate() ? "true" : "false");
        //element.setAttribute("turnoutType", p.getTurnoutType()?"double":"single");

        // new style
        element.addContent(storeIcon("lowerWestToUpperEast", p.getLowerWestToUpperEastIcon(), p.getLWUEText()));
        element.addContent(storeIcon("upperWestToLowerEast", p.getUpperWestToLowerEastIcon(), p.getUWLEText()));
        element.addContent(super.storeIcon("unknown", p.getUnknownIcon()));
        element.addContent(super.storeIcon("inconsistent", p.getInconsistentIcon()));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.SlipTurnoutIconXml");

        return element;
    }

    Element storeIcon(String elemName, NamedIcon icon, String text) {
        Element element = super.storeIcon(elemName, icon);
        element.addContent(new Element("text").addContent(text));
        return element;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor) o;

        SlipTurnoutIcon l = new SlipTurnoutIcon(p);
        int rotation = 0;
        try {
            Attribute a = element.getAttribute("rotate");
            rotation = a.getIntValue();
        } catch (org.jdom2.DataConversionException e) {
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        String nameEast = loadTurnout(element, "turnoutEast");
        /*try {
         nameEast=element.getAttribute("turnoutEast").getValue();
         } catch ( NullPointerException e) { 
         log.error("incorrect information for turnout; must use turnout name");
         return;
         }*/
        String nameWest = loadTurnout(element, "turnoutWest");
        /*try {
         nameWest=element.getAttribute("turnoutWest").getValue();
         } catch ( NullPointerException e) { 
         log.error("incorrect information for turnout; must use turnout name");
         return;
         }*/

        Attribute a = element.getAttribute("turnoutType");
        if (a != null) {
            if (a.getValue().equals("doubleSlip")) {
                l.setTurnoutType(SlipTurnoutIcon.DOUBLESLIP);
            } else if (a.getValue().equals("singleSlip")) {
                l.setTurnoutType(SlipTurnoutIcon.SINGLESLIP);
                a = element.getAttribute("singleSlipRoute");
                if ((a == null) || a.getValue().equals("upperWestToUpperEast")) {
                    l.setSingleSlipRoute(true);
                } else {
                    l.setSingleSlipRoute(false);
                }
            } else if (a.getValue().equals("threeWay")) {
                l.setTurnoutType(SlipTurnoutIcon.THREEWAY);
                a = element.getAttribute("firstTurnoutExit");
                if ((a == null) || a.getValue().equals("lower")) {
                    l.setSingleSlipRoute(false);
                } else {
                    l.setSingleSlipRoute(true);
                }
            } else if (a.getValue().equals("scissor")) {
                l.setTurnoutType(SlipTurnoutIcon.SCISSOR);
                if (loadTurnout(element, "turnoutLowerWest") == null) {
                    l.setSingleSlipRoute(true);
                } else {
                    //loadTurnout(element, "turnoutLowerEast");
                    l.setSingleSlipRoute(false);
                    l.setTurnout(loadTurnout(element, "turnoutLowerEast"), SlipTurnoutIcon.LOWEREAST);
                    l.setTurnout(loadTurnout(element, "turnoutLowerWest"), SlipTurnoutIcon.LOWERWEST);
                }

            }
        }
        loadTurnoutIcon("lowerWestToUpperEast", rotation, l, element, p);
        loadTurnoutIcon("upperWestToLowerEast", rotation, l, element, p);
        switch (l.getTurnoutType()) {
            case SlipTurnoutIcon.DOUBLESLIP:
                loadTurnoutIcon("lowerWestToLowerEast", rotation, l, element, p);
                loadTurnoutIcon("upperWestToUpperEast", rotation, l, element, p);
                break;
            default:
                loadTurnoutIcon("lowerWestToLowerEast", rotation, l, element, p);
                break;
        }

        loadTurnoutIcon("unknown", rotation, l, element, p);
        loadTurnoutIcon("inconsistent", rotation, l, element, p);

        a = element.getAttribute("tristate");
        if ((a == null) || a.getValue().equals("true")) {
            l.setTristate(true);
        } else {
            l.setTristate(false);
        }

        l.setTurnout(nameEast, SlipTurnoutIcon.EAST);
        l.setTurnout(nameWest, SlipTurnoutIcon.WEST);

        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }

    private String loadTurnout(Element element, String turn) {
        if (element != null) {
            Element e = element.getChild(turn);
            if (e != null) {
                return e.getText();
            }
        }
        return null;
    }

    private void loadTurnoutIcon(String state, int rotation, SlipTurnoutIcon l,
            Element element, Editor ed) {
        NamedIcon icon = loadIcon(l, state, element, "SlipTurnout \"" + l.getNameString() + "\" icon \"" + state + "\" ", ed);
        String textValue = null;

        if (icon != null) {
            if (state.equals("lowerWestToUpperEast")) {
                l.setLowerWestToUpperEastIcon(icon);
            } else if (state.equals("upperWestToLowerEast")) {
                l.setUpperWestToLowerEastIcon(icon);
            } else if (state.equals("lowerWestToLowerEast")) {
                l.setLowerWestToLowerEastIcon(icon);
            } else if (state.equals("upperWestToUpperEast")) {
                l.setUpperWestToUpperEastIcon(icon);
            } else if (state.equals("unknown")) {
                l.setUnknownIcon(icon);
            } else if (state.equals("inconsistent")) {
                l.setInconsistentIcon(icon);
            }
        } else {
            return;
        }
        Element elem = element.getChild(state);
        if (elem != null) {
            Element e = elem.getChild("text");
            if (e != null) {
                textValue = e.getText();
            }
        }
        if (textValue != null) {
            if (state.equals("lowerWestToUpperEast")) {
                l.setLWUEText(textValue);
            } else if (state.equals("upperWestToLowerEast")) {
                l.setUWLEText(textValue);
            } else if (state.equals("lowerWestToLowerEast")) {
                l.setLWLEText(textValue);
            } else if (state.equals("upperWestToUpperEast")) {
                l.setUWUEText(textValue);
            }
        }
    }
    private final static Logger log = LoggerFactory.getLogger(SlipTurnoutIconXml.class);
}
