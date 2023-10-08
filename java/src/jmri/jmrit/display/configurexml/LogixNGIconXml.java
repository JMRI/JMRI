package jmri.jmrit.display.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LogixNGIcon;
import jmri.jmrit.display.Positionable;

import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Handle configuration for display.LogixNGIcon objects.
 *
 * @author Daniel Bergqvist (C) 2023
 */
public class LogixNGIconXml extends PositionableLabelXml {

    /**
     * Default implementation for storing the contents of a LogixNGIcon
     *
     * @param o Object to store, of type LogixNGIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LogixNGIcon p = (LogixNGIcon) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("logixngicon");
        storeCommonAttributes(p, element);

        element.addContent(new Element("Identity").addContent(Integer.toString(p.getIdentity())));

        if (p.isText()) {
            if (p.getUnRotatedText() != null) {
                element.setAttribute("text", p.getUnRotatedText());
            }
            storeTextInfo(p, element);
        }

        if (p.isIcon() && p.getIcon() != null) {
            element.setAttribute("icon", "yes");
            element.addContent(storeIcon("icon", (NamedIcon) p.getIcon()));
        }

        storeLogixNG_Data(p, element);

        element.setAttribute("class", "jmri.jmrit.display.configurexml.LogixNGIconXml");
        return element;
    }

    /**
     * Create a LogixNGIcon, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        LogixNGIcon l = null;

        int identity = Integer.parseInt(element.getChildText("Identity"));

        // get object class and determine editor being used
        Editor editor = (Editor) o;
        if (element.getAttribute("icon") != null) {
            NamedIcon icon;
            String name = element.getAttribute("icon").getValue();
//            if (log.isDebugEnabled()) log.debug("icon attribute= "+name);
            if (name.equals("yes")) {
                icon = getNamedIcon("icon", element, "LogixNGIcon ", editor);
            } else {
                icon = NamedIcon.getIconByName(name);
                if (icon == null) {
                    icon = editor.loadFailed("LogixNGIcon", name);
                    if (icon == null) {
                        log.info("LogixNGIcon icon removed for url= {}", name);
                        return;
                    }
                }
            }
            // abort if name != yes and have null icon
            if (icon == null && !name.equals("yes")) {
                log.info("LogixNGIcon icon removed for url= {}", name);
                return;
            }
            l = new LogixNGIcon(identity, icon, editor);
            try {
                Attribute a = element.getAttribute("rotate");
                if (a != null && icon != null) {
                    int rotation = element.getAttribute("rotate").getIntValue();
                    icon.setRotation(rotation, l);
                }
            } catch (org.jdom2.DataConversionException e) {
            }

            if (name.equals("yes")) {
                NamedIcon nIcon = loadIcon(l, "icon", element, "LogixNGIcon ", editor);
                if (nIcon != null) {
                    l.updateIcon(nIcon);
                } else {
                    log.info("LogixNGIcon icon removed for url= {}", name);
                    return;
                }
            } else {
                l.updateIcon(icon);
            }
        }

        if (element.getAttribute("text") != null) {
            if (l == null) {
                l = new LogixNGIcon(identity, element.getAttribute("text").getValue(), editor);
            }
            loadTextInfo(l, element);

        } else if (l == null) {
            log.error("LogixNGIcon is null!");
            if (log.isDebugEnabled()) {
                java.util.List<Attribute> attrs = element.getAttributes();
                log.debug("\tElement Has {} Attributes:", attrs.size());
                for (Attribute a : attrs) {
                    log.debug("  attribute:  {} = {}", a.getName(), a.getValue());
                }
                java.util.List<Element> kids = element.getChildren();
                log.debug("\tElementHas {} children:", kids.size());
                for (Element e : kids) {
                    log.debug("  child:  {} = \"{}\"", e.getName(), e.getValue());
                }
            }
            editor.loadFailed();
            return;
        }
        try {
            editor.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            // This should never happen
            log.error("Editor.putItem() with null id has thrown DuplicateIdException", e);
        }

        loadLogixNG_Data(l, element);

        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.LABELS, element);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNGIconXml.class);
}
