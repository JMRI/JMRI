package apps.configurexml;

import apps.SystemConsoleConfigPanel;
import apps.systemconsole.SystemConsolePreferencesManager;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle XML persistence of SystemConsoleConfigPanel objects.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris copyright (c) 2010
 * @see apps.SystemConsoleConfigPanel
 */
public class SystemConsoleConfigPanelXml extends jmri.configurexml.AbstractXmlAdapter {

    public SystemConsoleConfigPanelXml() {
    }

    /**
     * Arrange for console settings to be stored
     *
     * @param o Object to store, of type SystemConsole
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        Element e = new Element("console");
        e.setAttribute("class", this.getClass().getName());
        SystemConsolePreferencesManager manager = InstanceManager.getDefault(SystemConsolePreferencesManager.class);
        e.setAttribute("scheme", "" + manager.getScheme());
        e.setAttribute("fontfamily", "" + manager.getFontFamily());
        e.setAttribute("fontsize", "" + manager.getFontSize());
        e.setAttribute("fontstyle", "" + manager.getFontStyle());
        e.setAttribute("wrapstyle", "" + manager.getWrapStyle());

        return e;
    }

    /**
     * Object should be loaded after basic GUI constructed
     *
     * @return true to defer loading
     * @see jmri.configurexml.AbstractXmlAdapter#loadDeferred()
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     */
    @Override
    public boolean loadDeferred() {
        return true;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        String value;
        SystemConsolePreferencesManager manager = InstanceManager.getDefault(SystemConsolePreferencesManager.class);

        try {
            if ((value = shared.getAttributeValue("scheme")) != null) {
                manager.setScheme(Integer.parseInt(value));
            }

            if ((value = shared.getAttributeValue("fontfamily")) != null) {
                manager.setFontFamily(value);
            }

            if ((value = shared.getAttributeValue("fontsize")) != null) {
                manager.setFontSize(Integer.parseInt(value));
            }

            if ((value = shared.getAttributeValue("fontstyle")) != null) {
                manager.setFontStyle(Integer.parseInt(value));
            }

            if ((value = shared.getAttributeValue("wrapstyle")) != null) {
                manager.setWrapStyle(Integer.parseInt(value));
            }

        } catch (NumberFormatException ex) {
            log.error("NumberFormatException while setting System Console parameters: " + ex);
            result = false;
        }

        // As we've had a load request, register the system console with the
        // preference manager
        ConfigureManager cm = jmri.InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerPref(new SystemConsoleConfigPanel());
        }

        return result;
    }

    /**
     * Update static data from XML file
     *
     * @param element Top level Element to unpack.
     * @param o       ignored
     */
    @Override
    public void load(Element element, Object o) {
        log.error("Unexpected call of load(Element, Object)");
    }
    // initialize logging
    private static final Logger log = LoggerFactory.getLogger(SystemConsoleConfigPanelXml.class);

}
