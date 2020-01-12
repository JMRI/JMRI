package jmri.jmrit.display.configurexml;

import java.util.HashMap;
import java.util.List;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.TurnoutIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.TurnoutIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class TurnoutIconXml extends PositionableLabelXml {

    static final HashMap<String, String> _nameMap = new HashMap<String, String>();

    public TurnoutIconXml() {
        // map previous store names to property key names
        _nameMap.put("closed", "TurnoutStateClosed");
        _nameMap.put("thrown", "TurnoutStateThrown");
        _nameMap.put("unknown", "BeanStateUnknown");
        _nameMap.put("inconsistent", "BeanStateInconsistent");
    }

    /**
     * Default implementation for storing the contents of a TurnoutIcon
     *
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        TurnoutIcon p = (TurnoutIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("turnouticon");
        element.setAttribute("turnout", p.getNamedTurnout().getName());
        storeCommonAttributes(p, element);

        element.setAttribute("tristate", p.getTristate() ? "true" : "false");
        element.setAttribute("momentary", p.getMomentary() ? "true" : "false");
        element.setAttribute("directControl", p.getDirectControl() ? "true" : "false");

        Element elem = new Element("icons");
        elem.addContent(storeIcon("closed", p.getIcon("TurnoutStateClosed")));
        elem.addContent(storeIcon("thrown", p.getIcon("TurnoutStateThrown")));
        elem.addContent(storeIcon("unknown", p.getIcon("BeanStateUnknown")));
        elem.addContent(storeIcon("inconsistent", p.getIcon("BeanStateInconsistent")));
        element.addContent(elem);
        elem = new Element("iconmaps");
        String family = p.getFamily();
        if (family != null) {
            elem.setAttribute("family", family);
        }
        element.addContent(elem);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.TurnoutIconXml");

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

        TurnoutIcon l = new TurnoutIcon(p);

        String name;
        try {
            name = element.getAttribute("turnout").getValue();
        } catch (NullPointerException e) {
            log.error("incorrect information for turnout; must use turnout name");
            p.loadFailed();
            return;
        }
        l.setTurnout(name);

        Attribute a = element.getAttribute("tristate");
        if ((a == null) || a.getValue().equals("true")) {
            l.setTristate(true);
        } else {
            l.setTristate(false);
        }

        a = element.getAttribute("momentary");
        if ((a != null) && a.getValue().equals("true")) {
            l.setMomentary(true);
        } else {
            l.setMomentary(false);
        }

        a = element.getAttribute("directControl");
        if ((a != null) && a.getValue().equals("true")) {
            l.setDirectControl(true);
        } else {
            l.setDirectControl(false);
        }

        List<Element> states = element.getChildren();
        if (states.size() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("Main element has" + states.size() + " items");
            }
            Element elem = element;     // the element containing the icons
            Element icons = element.getChild("icons");
            if (icons != null) {
                List<Element> s = icons.getChildren();
                states = s;
                elem = icons;          // the element containing the icons
                if (log.isDebugEnabled()) {
                    log.debug("icons element has" + states.size() + " items");
                }
            }
            for (int i = 0; i < states.size(); i++) {
                String state = states.get(i).getName();
                if (log.isDebugEnabled()) {
                    log.debug("setIcon for state \"" + state
                            + "\" and " + _nameMap.get(state));
                }
                NamedIcon icon = loadIcon(l, state, elem, "TurnoutIcon \"" + name + "\": icon \"" + state + "\" ", p);
                if (icon != null) {
                    l.setIcon(_nameMap.get(state), icon);
                } else {
                    log.info("TurnoutIcon \"" + name + "\": icon \"" + state + "\" removed");
                    return;
                }
            }
            log.debug(states.size() + " icons loaded for " + l.getNameString());
        } else {        // case when everything was attributes
            int rotation = 0;
            try {
                rotation = element.getAttribute("rotate").getIntValue();
            } catch (org.jdom2.DataConversionException e) {
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
            if (loadTurnoutIcon("thrown", rotation, l, element, name, p) == null) {
                return;
            }
            if (loadTurnoutIcon("closed", rotation, l, element, name, p) == null) {
                return;
            }
            if (loadTurnoutIcon("unknown", rotation, l, element, name, p) == null) {
                return;
            }
            if (loadTurnoutIcon("inconsistent", rotation, l, element, name, p) == null) {
                return;
            }
        }
        Element elem = element.getChild("iconmaps");
        if (elem != null) {
            Attribute attr = elem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
        }

        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }

    private NamedIcon loadTurnoutIcon(String state, int rotation, TurnoutIcon l, Element element,
            String name, Editor ed) {
        NamedIcon icon = null;
        if (element.getAttribute(state) != null) {
            String iconName = element.getAttribute(state).getValue();
            icon = NamedIcon.getIconByName(iconName);
            if (icon == null) {
                icon = ed.loadFailed("Turnout \"" + name + "\" icon \"" + state + "\" ", iconName);
                if (icon == null) {
                    log.info("Turnout \"" + name + "\" icon \"" + state + "\" removed for url= " + iconName);
                }
            } else {
                icon.setRotation(rotation, l);
            }
        } else {
            log.warn("did not locate " + state + " icon file for Turnout " + name);
        }
        if (icon == null) {
            log.info("Turnout Icon \"" + name + "\": icon \"" + state + "\" removed");
        } else {
            l.setIcon(_nameMap.get(state), icon);
        }
        return icon;
    }

    private final static Logger log = LoggerFactory.getLogger(TurnoutIconXml.class);
}
