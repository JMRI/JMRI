// CornwallRR.java

package apps.cornwall;

import apps.Apps;

import java.text.MessageFormat;

import javax.swing.JFrame;

/**
 * Nick Kulp's Cornwall Railroad.
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
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 */
public class CornwallRR extends Apps {

    protected String line1() {
        return MessageFormat.format("Cornwall RR, based on JMRI {0}",
                                new Object[]{jmri.Version.name()});
    }
    protected String line2() {
        return "http://jmri.org/Panels/Cornwall.html";
    }
    protected String logo() {
        return "resources/icons/cornwall/cornwall_logo.gif";
    }

    CornwallRR(JFrame p) {
        super(p);
        log.debug("CTOR done");
    }


    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        log.info(apps.Apps.startupInfo("CornwallRR"));

        setConfigFilename("CornwallConfig2.xml", args);
        JFrame f = new JFrame("Cornwall Railroad");
        createFrame(new CornwallRR(f), f);

        log.debug("main initialization done");
        splash(false);

        // start automation
        
        // load definitions
        loadFile("CornwallDefinitions.xml");

        // start automation (whith will work in parallel)
        new CrrInit().start();

        // show panel
        loadFile("CornwallMain.xml");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CornwallRR.class.getName());
}


