package jmri.jmrix.dccpp.swing.virtuallcd.configurexml;

import java.awt.Dimension;
import java.util.*;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.virtuallcd.VirtualLCDConfiguration;
import jmri.jmrix.dccpp.swing.virtuallcd.VirtualLCDConfiguration.DisplayConfig;

import org.jdom2.Element;

/**
 * Handle configuration for VirtualLCDConfiguration objects.
 *
 * @author Howard G. Penny  Copyright (c) 2005
 * @author Daniel Bergqvist Copyright (c) 2026
 */
public class VirtualLCDConfigurationXml {

    public static Element store(VirtualLCDConfiguration p) {

        Element element = new Element("virtual_lcd_config");

        if (p.getMemo() != null) {
            element.addContent(new Element("systemConnection")
                    .addContent(p.getMemo().getSystemPrefix()));
        }
        Dimension lcdSize = p.getLCDSize();
        if (lcdSize != null) {
            element.addContent(new Element("numColumns")
                    .addContent(Integer.toString(lcdSize.width)));
            element.addContent(new Element("numRows")
                    .addContent(Integer.toString(lcdSize.height)));
        }
        element.addContent(new Element("displayConfig")
                .addContent(p.getDisplayConfig().name()));
        element.addContent(new Element("displayNo")
                .addContent(Integer.toString(p.getDisplayNo())));
        element.addContent(new Element("minDisplayNo")
                .addContent(Integer.toString(p.getMinDisplayNo())));
        element.addContent(new Element("maxDisplayNo")
                .addContent(Integer.toString(p.getMaxDisplayNo())));

        Element selectedDisplays = new Element("selectedDisplays");
        for (int display : p.getSelectedDisplays()) {
            selectedDisplays.addContent(new Element("displayNo")
                    .addContent(Integer.toString(display)));
        }
        element.addContent(selectedDisplays);

        return element;
    }

    public static void load(VirtualLCDConfiguration p, Element elem)
            throws JmriConfigureXmlException {

        Element element = elem.getChild("virtual_lcd_config");

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

        p.setMemo(memo);

        Element numColumnsElement = element.getChild("numColumns");
        Element numRowsElement = element.getChild("numRows");
        if (numColumnsElement != null && numRowsElement != null) {
            int numColumns = Integer.parseInt(numColumnsElement.getTextTrim());
            int numRows = Integer.parseInt(numRowsElement.getTextTrim());
            p.setLCDSize(new Dimension(numColumns, numRows));
        }

        String displayConfigName = element.getChild("displayConfig").getTextTrim();
        p.setDisplayConfig(DisplayConfig.valueOf(displayConfigName));

        p.setDisplayNo(Integer.parseInt(element.getChild("displayNo").getTextTrim()));
        p.setMinDisplayNo(Integer.parseInt(element.getChild("minDisplayNo").getTextTrim()));
        p.setMaxDisplayNo(Integer.parseInt(element.getChild("maxDisplayNo").getTextTrim()));

        Set<Integer> selectedDisplaysSet = new HashSet<>();
        Element selectedDisplays = element.getChild("selectedDisplays");
        for (Element e : selectedDisplays.getChildren()) {
            selectedDisplaysSet.add(Integer.valueOf(e.getTextTrim()));
        }
        p.setSelectedDisplays(selectedDisplaysSet);
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLCDConfigurationXml.class);
}
