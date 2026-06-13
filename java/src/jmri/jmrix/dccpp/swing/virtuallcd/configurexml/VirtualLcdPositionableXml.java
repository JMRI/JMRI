package jmri.jmrix.dccpp.swing.virtuallcd.configurexml;

import jmri.jmrit.display.configurexml.*;


import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.*;
import jmri.jmrix.dccpp.swing.virtuallcd.VirtualLcdPositionable;

import org.jdom2.Element;

/**
 * Handle configuration for VirtualLcdPositionable objects.
 *
 * @author Howard G. Penny  Copyright (c) 2005
 * @author Daniel Bergqvist Copyright (c) 2026
 */
public class VirtualLcdPositionableXml
        extends PositionableLabelXml {

    public VirtualLcdPositionableXml() {
    }

    /**
     * Default implementation for storing the contents of an VirtualLcdPositionable
     *
     * @param o Object to store, of type VirtualLcdPositionable
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        VirtualLcdPositionable p = (VirtualLcdPositionable) o;
        if (!p.isActive()) {
            return null; // if flagged as inactive, don't store
        }

        Element element = new Element("dccex_virtual_lcd");

        element.addContent(VirtualLCDConfigurationXml.store(p.getVirtualLCDPanel()));

        // include contents
        if (p.getId() != null) element.setAttribute("id", p.getId());
        element.setAttribute("x", "" + p.getX());
        element.setAttribute("y", "" + p.getY());

        element.setAttribute("class", this.getClass().getName());

        storeLogixNG_Data(p, element);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create an VirtualLcdPositionable, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // get object class and create the clock object
        Editor ed = (Editor) o;

        VirtualLcdPositionable l = new VirtualLcdPositionable(ed);

        VirtualLCDConfigurationXml.load(l.getVirtualLCDPanel(), element, false);

        l.initComponents();

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            if (element.getAttribute("id") != null) {
                try {
                    l.setId(element.getAttribute("id").getValue());
                } catch (Positionable.DuplicateIdException e) {
                    throw new JmriConfigureXmlException("Positionable id is not unique", e);
                }
            }
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setOpaque(false);
        l.setLocation(x, y);

        // add the Virtual LCD to the panel
        l.setDisplayLevel(Editor.CLOCK);
        try {
            ed.putItem(l, true);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }

        loadLogixNG_Data(l, element);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLcdPositionableXml.class);
}
