// SignalHeadIconXml.java
package jmri.jmrit.display.configurexml;

import java.util.HashMap;
import java.util.List;
import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalHeadIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.SignalHeadIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public class SignalHeadIconXml extends PositionableLabelXml {

    static final java.util.ResourceBundle rbean = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");
    static final HashMap<String, String> _nameMap = new HashMap<String, String>();

    public SignalHeadIconXml() {
        // map previous store names to actual localized names
        _nameMap.put("red", rbean.getString("SignalHeadStateRed"));
        _nameMap.put("yellow", rbean.getString("SignalHeadStateYellow"));
        _nameMap.put("green", rbean.getString("SignalHeadStateGreen"));
        _nameMap.put("lunar", rbean.getString("SignalHeadStateLunar"));
        _nameMap.put("held", rbean.getString("SignalHeadStateHeld"));
        _nameMap.put("dark", rbean.getString("SignalHeadStateDark"));
        _nameMap.put("flashred", rbean.getString("SignalHeadStateFlashingRed"));
        _nameMap.put("flashyellow", rbean.getString("SignalHeadStateFlashingYellow"));
        _nameMap.put("flashgreen", rbean.getString("SignalHeadStateFlashingGreen"));
        _nameMap.put("flashlunar", rbean.getString("SignalHeadStateFlashingLunar"));
    }

    /**
     * Default implementation for storing the contents of a SignalHeadIcon
     *
     * @param o Object to store, of type SignalHeadIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        SignalHeadIcon p = (SignalHeadIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("signalheadicon");

        element.setAttribute("signalhead", "" + p.getNamedSignalHead().getName());
        storeCommonAttributes(p, element);
        element.setAttribute("clickmode", "" + p.getClickMode());
        element.setAttribute("litmode", "" + p.getLitMode());

        Element elem = new Element("icons");
        NamedIcon icon = p.getIcon(rbean.getString("SignalHeadStateHeld"));
        if (icon != null) {
            elem.addContent(storeIcon("held", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateDark"));
        if (icon != null) {
            elem.addContent(storeIcon("dark", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateRed"));
        if (icon != null) {
            elem.addContent(storeIcon("red", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateYellow"));
        if (icon != null) {
            elem.addContent(storeIcon("yellow", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateGreen"));
        if (icon != null) {
            elem.addContent(storeIcon("green", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateLunar"));
        if (icon != null) {
            elem.addContent(storeIcon("lunar", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateFlashingRed"));
        if (icon != null) {
            elem.addContent(storeIcon("flashred", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateFlashingYellow"));
        if (icon != null) {
            elem.addContent(storeIcon("flashyellow", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateFlashingGreen"));
        if (icon != null) {
            elem.addContent(storeIcon("flashgreen", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateFlashingLunar"));
        if (icon != null) {
            elem.addContent(storeIcon("flashlunar", icon));
        }
        element.addContent(elem);
        elem = new Element("iconmaps");
        String family = p.getFamily();
        if (family != null) {
            elem.setAttribute("family", family);
        }
        element.addContent(elem);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.SignalHeadIconXml");
        return element;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor) o;
        SignalHeadIcon l = new SignalHeadIcon(ed);
        String name;

        Attribute attr = element.getAttribute("signalhead");
        if (attr == null) {
            log.error("incorrect information for signal head; must use signalhead name");
            ed.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }

        SignalHead sh = jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name);

        if (sh != null) {
            l.setSignalHead(name);
        } else {
            log.error("SignalHead named '" + attr.getValue() + "' not found.");
            //    ed.loadFailed();
            return;
        }
        int rotation = 0;
        try {
            attr = element.getAttribute("rotate");
            rotation = attr.getIntValue();
        } catch (org.jdom2.DataConversionException e) {
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        List<Element> aspects = element.getChildren();
        if (aspects.size() > 0) {
            Element icons = element.getChild("icons");
            Element elem = element;
            if (icons != null) {
                List<Element> c = icons.getChildren();
                aspects = c;
                elem = icons;
            }
            for (int i = 0; i < aspects.size(); i++) {
                String aspect = aspects.get(i).getName();
                NamedIcon icon = loadIcon(l, aspect, elem, "SignalHead \"" + name + "\": icon \"" + aspect + "\" ", ed);
                if (icon != null) {
                    l.setIcon(_nameMap.get(aspect), icon);
                } else {
                    log.info("SignalHead \"" + name + "\": icon \"" + aspect + "\" removed");
                }
            }
            log.debug(aspects.size() + " icons loaded for " + l.getNameString());
        } else {
            // old style as attributes - somewhere around pre 2.5.4
            NamedIcon icon = loadSignalIcon("red", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateRed"), icon);
            }
            icon = loadSignalIcon("yellow", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateYellow"), icon);
            }
            icon = loadSignalIcon("green", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateGreen"), icon);
            }
            icon = loadSignalIcon("lunar", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateLunar"), icon);
            }
            icon = loadSignalIcon("held", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateHeld"), icon);
            }
            icon = loadSignalIcon("dark", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateDark"), icon);
            }
            icon = loadSignalIcon("flashred", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateFlashingRed"), icon);
            }
            icon = loadSignalIcon("flashyellow", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateFlashingYellow"), icon);
            }
            icon = loadSignalIcon("flashgreen", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateFlashingGreen"), icon);
            }
            icon = loadSignalIcon("flashlunar", rotation, l, element, name, ed);
            if (icon != null) {
                l.setIcon(rbean.getString("SignalHeadStateFlashingLunar"), icon);
            }
        }
        Element elem = element.getChild("iconmaps");
        if (elem != null) {
            attr = elem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
        }
        try {
            attr = element.getAttribute("clickmode");
            if (attr != null) {
                l.setClickMode(attr.getIntValue());
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("Failed on clickmode attribute: " + e);
        }

        try {
            attr = element.getAttribute("litmode");
            if (attr != null) {
                l.setLitMode(attr.getBooleanValue());
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("Failed on litmode attribute: " + e);
        }

        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.SIGNALS, element);
    }

    private NamedIcon loadSignalIcon(String aspect, int rotation, SignalHeadIcon l,
            Element element, String name, Editor ed) {
        String msg = "SignalHead \"" + name + "\": icon \"" + aspect + "\" ";
        NamedIcon icon = loadIcon(l, aspect, element, msg, ed);
        if (icon == null) {
            if (element.getAttribute(aspect) != null) {
                String iconName = element.getAttribute(aspect).getValue();
                icon = NamedIcon.getIconByName(iconName);
                if (icon == null) {
                    icon = ed.loadFailed(msg, iconName);
                    if (icon == null) {
                        log.info(msg + " removed for url= " + iconName);
                    }
                }
                if (icon != null) {
                    icon.setRotation(rotation, l);
                }
            } else {
                log.info("did not load file aspect " + aspect + " for SignalHead " + name);
            }
        }
        if (icon == null) {
            log.info("SignalHead Icon \"" + name + "\": icon \"" + aspect + "\" removed");
        }
        return icon;
    }

    private final static Logger log = LoggerFactory.getLogger(SignalHeadIconXml.class.getName());
}
