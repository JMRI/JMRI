package jmri.jmrix.dccpp.swing.virtuallcd.configurexml;

import jmri.jmrit.display.configurexml.*;

import java.util.List;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.*;
import jmri.jmrix.dccpp.swing.virtuallcd.VirtualLcdPositionable;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.VirtualLcdPositionable objects.
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
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        VirtualLcdPositionable p = (VirtualLcdPositionable) o;
        if (!p.isActive()) {
            return null; // if flagged as inactive, don't store
        }

        Element element = new Element("dccex_virtual_lcd");

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }
        element.addContent(new Element("displayNo")
                .addContent(Integer.toString(p.getDisplayNo())));

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
        DCCppSystemConnectionMemo memo = null;

        List<DCCppSystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(DCCppSystemConnectionMemo.class);

        String systemConnectionName = "Unknown connection";

        Element systemConnection = element.getChild("systemConnection");
        if (systemConnection != null) {
            systemConnectionName = systemConnection.getTextTrim();

            for (DCCppSystemConnectionMemo m : systemConnections) {
                if (m.getSystemPrefix().equals(systemConnectionName)) {
                    memo = m;
                    break;
                }
            }
        }

        if (memo == null) {
            throw new JmriConfigureXmlException("Cannot find connection: " + systemConnectionName);
        }

        Element displayNoElement = element.getChild("displayNo");
        int displayNo = Integer.parseInt(displayNoElement.getTextTrim());

        VirtualLcdPositionable l = new VirtualLcdPositionable(ed, memo, displayNo);

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

    private static final Logger log = LoggerFactory.getLogger(VirtualLcdPositionableXml.class);
}
