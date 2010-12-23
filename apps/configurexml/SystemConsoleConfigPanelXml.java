// SystemConsoleConfigPanelXml.java

package apps.configurexml;

import apps.SystemConsole;
import apps.SystemConsoleConfigPanel;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import org.jdom.Element;

/**
 * Handle XML persistance of SystemConsoleConfigPanel objects.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <P>
 *
 * @author Matthew Harris  copyright (c) 2010
 * @version $Revision: 1.5 $
 * @see apps.SystemConsoleConfigPanel
 */
public class SystemConsoleConfigPanelXml extends jmri.configurexml.AbstractXmlAdapter {

    public SystemConsoleConfigPanelXml() {
    }

    /**
     * Arrange for console settings to be stored
     * @param o Object to store, of type SystemConsole
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        Element ce = null;
        Element e = new Element("console");
        e.setAttribute("class", this.getClass().getName());
        e.setAttribute("scheme", ""+SystemConsole.getScheme());
        e.setAttribute("fontsize", ""+SystemConsole.getFontSize());
        e.setAttribute("fontstyle", ""+SystemConsole.getFontStyle());
        e.setAttribute("wrapstyle", ""+SystemConsole.getWrapStyle());

        if (SystemConsoleConfigPanel.isPositionSaved()) {
            ce = new Element("position");
            ce.setAttribute("x", ""+SystemConsole.getConsole().getX());
            ce.setAttribute("y", ""+SystemConsole.getConsole().getY());
            e.addContent(ce);
        }

        if (SystemConsoleConfigPanel.isSizeSaved()) {
            ce = new Element("size");
            ce.setAttribute("width", ""+SystemConsole.getConsole().getWidth());
            ce.setAttribute("height", ""+SystemConsole.getConsole().getHeight());
            e.addContent(ce);
        }

        return e;
    }

    /**
     * Object should be loaded after basic GUI constructed
     * @return true to defer loadng
     * @see jmri.configurexml.AbstractXmlAdapter#loadDeferred()
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     */
    @Override
    public boolean loadDeferred() {
        return true;
    }

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
        boolean result = true;
        String value;
        Element ce;

        try {
            if ((value = e.getAttributeValue("scheme"))!=null) {
                SystemConsole.setScheme(Integer.parseInt(value));
            }

            if ((value = e.getAttributeValue("fontsize"))!=null) {
                SystemConsole.setFontSize(Integer.parseInt(value));
            }

            if ((value = e.getAttributeValue("fontstyle"))!=null) {
                SystemConsole.setFontStyle(Integer.parseInt(value));
            }

            if ((value = e.getAttributeValue("wrapstyle"))!=null) {
                SystemConsole.setWrapStyle(Integer.parseInt(value));
            }

            if ((ce = e.getChild("position"))!=null) {
                SystemConsoleConfigPanel.setPositionSaved(true);
                boolean onScreen = false;

                // Retrieve stored co-ordinates
                int x = Integer.parseInt(ce.getAttributeValue("x"));
                int y = Integer.parseInt(ce.getAttributeValue("y"));

                // Check if stored co-ordinates are valid for at least one screen
                for (GraphicsDevice gd: GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
                    onScreen = gd.getDefaultConfiguration().getBounds().contains(x, y)?true:onScreen;
                }

                if (!onScreen) {
                    // Set to default position (0,0)
                    log.warn("Stored console position is off-screen (" + x + ", " + y + ") - reset to default (0, 0)");
                    x = y = 0;
                }

                // Finally, set the console location
                SystemConsole.getConsole().setLocation(x, y);
            }

            if ((ce = e.getChild("size"))!=null) {
                SystemConsoleConfigPanel.setSizeSaved(true);
                SystemConsole.getConsole().setSize(
                        Integer.parseInt(ce.getAttributeValue("width")),
                        Integer.parseInt(ce.getAttributeValue("height")));
            }

            result = true;
        } catch (Exception ex) {
            log.error("Exception while setting System Console parameters: "+ex);
            result = false;
        }

        // As we've had a load request, register the system console with the
        // preference manager
        jmri.InstanceManager.configureManagerInstance().registerPref(new SystemConsoleConfigPanel());

        return result;
    }

    /**
     * Update static data from XML file
     * @param element Top level Element to unpack.
     * @param o  ignored
     */
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SystemConsoleConfigPanelXml.class.getName());

}