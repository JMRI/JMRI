package jmri.jmrit.display.configurexml;

import jmri.SignalMast;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalMastIcon;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.SignalMastIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2010
 */
public class SignalMastIconXml extends PositionableLabelXml {

    public SignalMastIconXml() {
    }

    /**
     * Default implementation for storing the contents of a SignalMastIcon
     *
     * @param o Object to store, of type SignalMastIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        SignalMastIcon p = (SignalMastIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("signalmasticon");
        element.setAttribute("signalmast", "" + p.getNamedSignalMast().getName());
        storeCommonAttributes(p, element);
        element.setAttribute("clickmode", "" + p.getClickMode());
        element.setAttribute("litmode", "" + p.getLitMode());
        element.setAttribute("degrees", String.valueOf(p.getDegrees()));
        element.setAttribute("scale", String.valueOf(p.getScale()));
        element.setAttribute("imageset", p.useIconSet());
        element.setAttribute("class", "jmri.jmrit.display.configurexml.SignalMastIconXml");
        //storeIconInfo(p, element);
        return element;
    }

    /**
     * Create a SignalMastIcon, then add
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor) o;
        SignalMastIcon l = new SignalMastIcon(ed);
        String name;

        Attribute attr;
        /*
         * We need to set the rotation and scaling first, prior to setting the
         * signalmast, otherwise we end up in a situation where by the icons do
         * not get rotated or scaled correctly.
         **/
        try {
            int rotation = 0;
            double scale = 1.0;
            attr = element.getAttribute("rotation");    // former attribute name.
            if (attr != null) {
                rotation = attr.getIntValue();
            }
            attr = element.getAttribute("degrees");
            if (attr != null) {
                rotation = attr.getIntValue();
            }
            l.rotate(rotation);
            attr = element.getAttribute("scale");
            String text = "Error attr null";
            if (attr != null) {
                scale = attr.getDoubleValue();
                text = attr.getValue();
            }
            l.setScale(scale);
            if (log.isDebugEnabled()) {
                log.debug("Load SignalMast rotation= " + rotation
                        + " scale= " + scale + " attr text= " + text);
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert rotation or scale attribute");
        }
        attr = element.getAttribute("signalmast");
        if (attr == null) {
            log.error("incorrect information for signal mast; must use signalmast name");
            ed.loadFailed();
            return;
        } else {
            name = attr.getValue();
            if (log.isDebugEnabled()) {
                log.debug("Load SignalMast " + name);
            }
        }

        SignalMast sh = jmri.InstanceManager.getDefault(jmri.SignalMastManager.class).getSignalMast(name);

        if (sh != null) {
            l.setSignalMast(name);
        } else {
            log.error("SignalMast named '" + attr.getValue() + "' not found.");
            ed.loadFailed();
            //    return;
        }

        attr = element.getAttribute("imageset");
        if (attr != null) {
            l.useIconSet(attr.getValue());
        }

        attr = element.getAttribute("imageset");
        if (attr != null) {
            l.useIconSet(attr.getValue());
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

    private final static Logger log = LoggerFactory.getLogger(SignalMastIconXml.class);

}
