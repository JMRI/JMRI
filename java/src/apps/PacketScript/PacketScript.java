/**
 * PacketScript.java
 */

package apps.PacketScript;


import apps.Apps;
import java.text.MessageFormat;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main program for the NMRA PacketScript program based on JMRI.
 * <P>
 * If an argument is provided at startup, it will be used as the name of
 * the configuration file.  Note that this is just the name, not the path;
 * the file is searched for in the usual way, first in the preferences tree and then in
 * xml/
 *
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
 * @author	Bob Jacobsen   Copyright 2002
 * @version     $Revision$
 */
public class PacketScript extends Apps {

    PacketScript(JFrame p) {
        super(p);
        }

    @Override
    protected void createMenus(JMenuBar menuBar, WindowInterface wi) {
        fileMenu(menuBar, wi);
        editMenu(menuBar, wi);
        toolsMenu(menuBar, wi);
        panelMenu(menuBar, wi);
        systemsMenu(menuBar, wi);
        debugMenu(menuBar, wi);
    }

    protected String line1() {
        return MessageFormat.format(Bundle.getMessage("PacketScriptVersionCredit"),
                                new Object[]{jmri.Version.name()});
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        Apps.setStartupInfo("PacketScript");

        setConfigFilename("PacketScriptConfig2.xml", args);
        JFrame f = new JFrame("PanelPro");
        createFrame(new PacketScript(f), f);

        log.debug("main initialization done");
        splash(false);
    }

    static Logger log = LoggerFactory.getLogger(PacketScript.class.getName());
}


