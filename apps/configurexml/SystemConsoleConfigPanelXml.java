// SystemConsoleConfigPanelXml.java

package apps.configurexml;

import apps.SystemConsole;
import apps.SystemConsoleConfigPanel;
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
 * @version $Revision: 1.1 $
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

        if (SystemConsoleConfigPanel.isPositionSaved()) {
            ce = new Element("position");
            ce.setAttribute("x", ""+SystemConsole.getConsole().getLocation().x);
            ce.setAttribute("y", ""+SystemConsole.getConsole().getLocation().y);
            e.addContent(ce);
        }

        if (SystemConsoleConfigPanel.isSizeSaved()) {
            ce = new Element("size");
            ce.setAttribute("width", ""+SystemConsole.getConsole().getSize().width);
            ce.setAttribute("height", ""+SystemConsole.getConsole().getSize().height);
            e.addContent(ce);
        }

        return e;
    }

    /**
     * Update static data from XML file
     * @param e Top level Element to unpack.
     * @return true if successful
      */
    public boolean load(Element e) {
        String value;
        Element ce;

        try {
            SystemConsole.setScheme(Integer.parseInt(e.getAttributeValue("scheme")));

            if ((value = e.getAttributeValue("fontsize"))!=null) {
                SystemConsole.setFontSize(Integer.parseInt(value));
            }

            if ((value = e.getAttributeValue("fontstyle"))!=null) {
                SystemConsole.setFontStyle(Integer.parseInt(value));
            }

            if ((ce = e.getChild("position"))!=null) {
                SystemConsoleConfigPanel.setPositionSaved(true);
                SystemConsole.getConsole().setLocation(
                        Integer.parseInt(ce.getAttributeValue("x")),
                        Integer.parseInt(ce.getAttributeValue("y")));
            }

            if ((ce = e.getChild("size"))!=null) {
                SystemConsoleConfigPanel.setSizeSaved(true);
                SystemConsole.getConsole().setSize(
                        Integer.parseInt(ce.getAttributeValue("width")),
                        Integer.parseInt(ce.getAttributeValue("height")));
            }

            return true;
        } catch (Exception ex) {
            log.error("Exception while setting System Console parameters: "+ex);
            return false;
        }
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