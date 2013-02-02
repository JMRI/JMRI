// SystemConsoleConfigPanelXml.java

package apps.configurexml;

import org.apache.log4j.Logger;
import apps.SystemConsole;
import apps.SystemConsoleConfigPanel;
import jmri.util.swing.FontComboUtil;
import org.jdom.Element;

/**
 * Handle XML persistence of SystemConsoleConfigPanel objects.
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
 * @version $Revision$
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
    @Override
    public Element store(Object o) {

        Element e = new Element("console");
        e.setAttribute("class", this.getClass().getName());
        e.setAttribute("scheme", ""+SystemConsole.getInstance().getScheme());
        e.setAttribute("fontfamily", ""+SystemConsole.getInstance().getFontFamily());
        e.setAttribute("fontsize", ""+SystemConsole.getInstance().getFontSize());
        e.setAttribute("fontstyle", ""+SystemConsole.getInstance().getFontStyle());
        e.setAttribute("wrapstyle", ""+SystemConsole.getInstance().getWrapStyle());

        return e;
    }

    /**
     * Object should be loaded after basic GUI constructed
     * @return true to defer loading
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
    @Override
    public boolean load(Element e) {
        boolean result = true;
        String value;

        try {
            if ((value = e.getAttributeValue("scheme"))!=null) {
                SystemConsole.getInstance().setScheme(Integer.parseInt(value));
            }

            if ((value = e.getAttributeValue("fontfamily"))!=null) {
                
                // Check if stored font family exists
                if (!FontComboUtil.getFonts(FontComboUtil.MONOSPACED).contains(value)) {

                    // No - reset to default
                    log.warn("Stored console font is not compatible (" + value + ") - reset to default (Monospaced)");
                    value = "Monospaced";
                }

                // Finally, set the font family
                SystemConsole.getInstance().setFontFamily(value);
            }

            if ((value = e.getAttributeValue("fontsize"))!=null) {
                SystemConsole.getInstance().setFontSize(Integer.parseInt(value));
            }

            if ((value = e.getAttributeValue("fontstyle"))!=null) {
                SystemConsole.getInstance().setFontStyle(Integer.parseInt(value));
            }

            if ((value = e.getAttributeValue("wrapstyle"))!=null) {
                SystemConsole.getInstance().setWrapStyle(Integer.parseInt(value));
            }

        } catch (NumberFormatException ex) {
            log.error("NumberFormatException while setting System Console parameters: "+ex);
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
    @Override
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    private static final Logger log = Logger.getLogger(SystemConsoleConfigPanelXml.class.getName());

}
