package jmri.jmrit.display.configurexml;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SensorIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.SensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class SensorIconXml extends PositionableLabelXml {

    static final HashMap<String, String> _nameMap = new HashMap<String, String>();

    public SensorIconXml() {
        // map previous store names to property key names
        _nameMap.put("active", "SensorStateActive");
        _nameMap.put("inactive", "SensorStateInactive");
        _nameMap.put("unknown", "BeanStateUnknown");
        _nameMap.put("inconsistent", "BeanStateInconsistent");
    }

    /**
     * Default implementation for storing the contents of a SensorIcon
     *
     * @param o Object to store, of type SensorIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        SensorIcon p = (SensorIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("sensoricon");
        element.setAttribute("sensor", p.getNamedSensor().getName());
        storeCommonAttributes(p, element);
        element.setAttribute("momentary", p.getMomentary() ? "true" : "false");
        element.setAttribute("icon", p.isIcon() ? "yes" : "no");

        storeIconInfo(p, element);
        storeTextInfo(p, element);
        element.setAttribute("class", "jmri.jmrit.display.configurexml.SensorIconXml");
        return element;
    }

    protected void storeTextInfo(SensorIcon p, Element element) {
        if (p.getText() == null) {
            String s = p.getOriginalText();
            if (s != null && s.length() > 0) {
                element.setAttribute("text", s);
            } else if (p.isText()) {
                element.setAttribute("text", "");
            } else {
                return;
            }
        } else {
            element.setAttribute("text", p.getText());
        }
        // get iconic overlay text info
        super.storeTextInfo(p, element);
        // get state textual info
        Element textElement = new Element("activeText");
        if (p.getActiveText() != null) {
            textElement.setAttribute("text", p.getActiveText());
        }

        // always write text color
        textElement.setAttribute("red", "" + p.getTextActive().getRed());
        textElement.setAttribute("green", "" + p.getTextActive().getGreen());
        textElement.setAttribute("blue", "" + p.getTextActive().getBlue());

        if (p.getBackgroundActive() != null) {
            textElement.setAttribute("redBack", "" + p.getBackgroundActive().getRed());
            textElement.setAttribute("greenBack", "" + p.getBackgroundActive().getGreen());
            textElement.setAttribute("blueBack", "" + p.getBackgroundActive().getBlue());
        }
        element.addContent(textElement);
        textElement = new Element("inactiveText");
        if (p.getInactiveText() != null) {
            textElement.setAttribute("text", p.getInactiveText());
        }

        // always write text color
        textElement.setAttribute("red", "" + p.getTextInActive().getRed());
        textElement.setAttribute("green", "" + p.getTextInActive().getGreen());
        textElement.setAttribute("blue", "" + p.getTextInActive().getBlue());

        if (p.getBackgroundInActive() != null) {
            textElement.setAttribute("redBack", "" + p.getBackgroundInActive().getRed());
            textElement.setAttribute("greenBack", "" + p.getBackgroundInActive().getGreen());
            textElement.setAttribute("blueBack", "" + p.getBackgroundInActive().getBlue());
        }
        element.addContent(textElement);

        textElement = new Element("unknownText");

        if (p.getUnknownText() != null) {
            textElement.setAttribute("text", p.getUnknownText());
        }

        // always write text color
        textElement.setAttribute("red", "" + p.getTextUnknown().getRed());
        textElement.setAttribute("green", "" + p.getTextUnknown().getGreen());
        textElement.setAttribute("blue", "" + p.getTextUnknown().getBlue());

        if (p.getBackgroundUnknown() != null) {
            textElement.setAttribute("redBack", "" + p.getBackgroundUnknown().getRed());
            textElement.setAttribute("greenBack", "" + p.getBackgroundUnknown().getGreen());
            textElement.setAttribute("blueBack", "" + p.getBackgroundUnknown().getBlue());
        }
        element.addContent(textElement);

        textElement = new Element("inconsistentText");
        if (p.getInconsistentText() != null) {
            textElement.setAttribute("text", p.getInconsistentText());
        }

        // always write text color
        textElement.setAttribute("red", "" + p.getTextInconsistent().getRed());
        textElement.setAttribute("green", "" + p.getTextInconsistent().getGreen());
        textElement.setAttribute("blue", "" + p.getTextInconsistent().getBlue());

        if (p.getBackgroundInconsistent() != null) {
            textElement.setAttribute("redBack", "" + p.getBackgroundInconsistent().getRed());
            textElement.setAttribute("greenBack", "" + p.getBackgroundInconsistent().getGreen());
            textElement.setAttribute("blueBack", "" + p.getBackgroundInconsistent().getBlue());
        }
        element.addContent(textElement);
    }

    protected void storeIconInfo(SensorIcon p, Element element) {
        element.addContent(storeIcon("active", p.getIcon("SensorStateActive")));
        element.addContent(storeIcon("inactive", p.getIcon("SensorStateInactive")));
        element.addContent(storeIcon("unknown", p.getIcon("BeanStateUnknown")));
        element.addContent(storeIcon("inconsistent", p.getIcon("BeanStateInconsistent")));
        Element elem = new Element("iconmaps");
        String family = p.getFamily();
        if (family != null) {
            elem.setAttribute("family", family);
        }
        element.addContent(elem);
    }

    boolean _icon;

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        Editor ed = (Editor) o;
        SensorIcon l;
        String name;
        Attribute attr = element.getAttribute("sensor");
        if (attr == null) {
            log.error("incorrect information for sensor; must use sensor name");
            ed.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }
        _icon = true;
        if (element.getAttribute("icon") != null) {
            String yesno = element.getAttribute("icon").getValue();
            if ((yesno != null) && (!yesno.equals(""))) {
                if (yesno.equals("yes")) {
                    _icon = true;
                } else if (yesno.equals("no")) {
                    _icon = false;
                }
            }
        }

        if (_icon) {
            l = new SensorIcon(new NamedIcon("resources/icons/smallschematics/tracksegments/circuit-error.gif",
                    "resources/icons/smallschematics/tracksegments/circuit-error.gif"),
                    ed);
        } else {
            l = new SensorIcon("  ", ed);
        }
        int rotation = 0;
        try {
            rotation = element.getAttribute("rotate").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        if (loadSensorIcon("active", rotation, l, element, name, ed) == null) {
            return;
        }
        if (loadSensorIcon("inactive", rotation, l, element, name, ed) == null) {
            return;
        }
        if (loadSensorIcon("unknown", rotation, l, element, name, ed) == null) {
            return;
        }
        if (loadSensorIcon("inconsistent", rotation, l, element, name, ed) == null) {
            return;
        }
        Element elem = element.getChild("iconmaps");
        if (elem != null) {
            attr = elem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
        }

        Attribute a = element.getAttribute("momentary");
        if ((a != null) && a.getValue().equals("true")) {
            l.setMomentary(true);
        } else {
            l.setMomentary(false);
        }

        loadTextInfo(l, element);
        l.setSensor(name);

        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.SENSORS, element);
        if (l.isIcon() && l.getText()!=null) {
            l.setOpaque(false);            
        }
    }

    private NamedIcon loadSensorIcon(String state, int rotation, SensorIcon l,
            Element element, String name, Editor ed) {
        String msg = "SensorIcon \"" + name + "\": icon \"" + state + "\" ";
        // loadIcon gets icon as an element
        NamedIcon icon = loadIcon(l, state, element, msg, ed);
        if (icon == null && _icon) {
            // old config files may define icons as attributes
            String iconName;
            if (element.getAttribute(state) != null
                    && !(iconName = element.getAttribute(state).getValue()).equals("")) {

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
                log.warn("did not locate " + state + " icon file for " + name);
            }
        }
        if (icon == null) {
            log.info(msg + " removed");
        } else {
            l.setIcon(_nameMap.get(state), icon);
        }
        return icon;
    }

    void loadTextInfo(SensorIcon l, Element element) {
        super.loadTextInfo(l, element);

        loadSensorTextState("Active", l, element);
        loadSensorTextState("InActive", l, element);
        loadSensorTextState("Unknown", l, element);
        loadSensorTextState("Inconsistent", l, element);
        if (element.getAttribute("text") != null) {
            l.setOriginalText(element.getAttribute("text").getValue());
            l.setText(element.getAttribute("text").getValue());
        }
    }

    private void loadSensorTextState(String state, SensorIcon l, Element element) {
        String name = null;
        Color clrText = null;
        Color clrBackground = null;
        List<Element> textList = element.getChildren(state.toLowerCase() + "Text");
        if (log.isDebugEnabled()) {
            log.debug("Found " + textList.size() + " " + state + "Text objects");
        }
        if (textList.size() > 0) {
            Element elem = textList.get(0);
            try {
                name = elem.getAttribute("text").getValue();
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = elem.getAttribute("red").getIntValue();
                int blue = elem.getAttribute("blue").getIntValue();
                int green = elem.getAttribute("green").getIntValue();
                clrText = new Color(red, green, blue);
            } catch (org.jdom2.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = elem.getAttribute("redBack").getIntValue();
                int blue = elem.getAttribute("blueBack").getIntValue();
                int green = elem.getAttribute("greenBack").getIntValue();
                clrBackground = new Color(red, green, blue);
            } catch (org.jdom2.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }

        } else {
            if (element.getAttribute(state.toLowerCase()) != null) {
                name = element.getAttribute(state.toLowerCase()).getValue();
            }
            try {
                int red = element.getAttribute("red" + state).getIntValue();
                int blue = element.getAttribute("blue" + state).getIntValue();
                int green = element.getAttribute("green" + state).getIntValue();
                clrText = new Color(red, green, blue);
            } catch (org.jdom2.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
            try {
                int red = element.getAttribute("red" + state + "Back").getIntValue();
                int blue = element.getAttribute("blue" + state + "Back").getIntValue();
                int green = element.getAttribute("green" + state + "Back").getIntValue();
                clrBackground = new Color(red, green, blue);
            } catch (org.jdom2.DataConversionException e) {
                log.warn("Could not parse color attributes!");
            } catch (NullPointerException e) {  // considered normal if the attributes are not present
            }
        }
        if (state.equals("Active")) {
            if (name != null) {
                l.setActiveText(name);
            }
            if (clrText != null) {
                l.setTextActive(clrText);
            }
            if (clrBackground != null) {
                l.setBackgroundActive(clrBackground);
            }
        } else if (state.equals("InActive")) {
            if (name != null) {
                l.setInactiveText(name);
            }
            if (clrText != null) {
                l.setTextInActive(clrText);
            }
            if (clrBackground != null) {
                l.setBackgroundInActive(clrBackground);
            }
        } else if (state.equals("Unknown")) {
            if (name != null) {
                l.setUnknownText(name);
            }
            if (clrText != null) {
                l.setTextUnknown(clrText);
            }
            if (clrBackground != null) {
                l.setBackgroundUnknown(clrBackground);
            }
        } else if (state.equals("Inconsistent")) {
            if (name != null) {
                l.setInconsistentText(name);
            }
            if (clrText != null) {
                l.setTextInconsistent(clrText);
            }
            if (clrBackground != null) {
                l.setBackgroundInconsistent(clrBackground);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SensorIconXml.class);

}
