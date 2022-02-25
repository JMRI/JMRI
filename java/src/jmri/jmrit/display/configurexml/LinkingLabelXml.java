package jmri.jmrit.display.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.*;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.LinkingLabel objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2013
 */
public class LinkingLabelXml extends PositionableLabelXml {

    public LinkingLabelXml() {
        super();
    }

    /**
     * Default implementation for storing the contents of a LinkingLabel
     *
     * @param o Object to store, of type LinkingLabel
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LinkingLabel p = (LinkingLabel) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("linkinglabel");
        storeCommonAttributes(p, element);

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

        element.addContent(new Element("url").addContent(p.getURL()));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.LinkingLabelXml");
        return element;
    }

    /**
     * Create LinkingLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        LinkingLabel l;

        String url = element.getChild("url").getText();

        // get object class and determine editor being used
        Editor editor = (Editor) o;
        if (element.getAttribute("icon") != null) {
            NamedIcon icon;
            String name = element.getAttribute("icon").getValue();
//            if (log.isDebugEnabled()) log.debug("icon attribute= "+name);
            if (name.equals("yes")) {
                icon = getNamedIcon("icon", element, "LinkingLabel ", editor);
            } else {
                icon = NamedIcon.getIconByName(name);
                if (icon == null) {
                    icon = editor.loadFailed("LinkingLabel", name);
                    if (icon == null) {
                        log.info("LinkingLabel icon removed for url= {}", name);
                        return;
                    }
                }
            }
            // abort if name != yes and have null icon
            if (icon == null && !name.equals("yes")) {
                log.info("LinkingLabel icon removed for url= {}", name);
                return;
            }
            l = new LinkingLabel(icon, editor, url);
            l.setPopupUtility(null);        // no text
            try {
                Attribute a = element.getAttribute("rotate");
                if (a != null && icon != null) {
                    int rotation = element.getAttribute("rotate").getIntValue();
                    icon.setRotation(rotation, l);
                }
            } catch (org.jdom2.DataConversionException e) {
            }

            if (name.equals("yes")) {
                NamedIcon nIcon = loadIcon(l, "icon", element, "LinkingLabel ", editor);
                if (nIcon != null) {
                    l.updateIcon(nIcon);
                } else {
                    log.info("LinkingLabel icon removed for url= {}", name);
                    return;
                }
            } else {
                l.updateIcon(icon);
            }

            //l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        } else if (element.getAttribute("text") != null) {
            l = new LinkingLabel(element.getAttribute("text").getValue(), editor, url);
            loadTextInfo(l, element);

        } else {
            log.error("LinkingLabel is null!");
            if (log.isDebugEnabled()) {
                java.util.List<Attribute> attrs = element.getAttributes();
                log.debug("\tElement Has {} Attributes:", attrs.size());
                for (Attribute a : attrs) {
                    log.debug("\tattribute:\t{} = {}", a.getName(), a.getValue());
                }
                java.util.List<Element> kids = element.getChildren();
                log.debug("\tElementHas {} children:", kids.size());
                for (Element e : kids) {
                    log.debug("\tchild:\t{} = \"{}\"", e.getName(), e.getValue());
                }
            }
            editor.loadFailed();
            return;
        }
        try {
            editor.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.LABELS, element);
    }

    private final static Logger log = LoggerFactory.getLogger(LinkingLabelXml.class);
}
