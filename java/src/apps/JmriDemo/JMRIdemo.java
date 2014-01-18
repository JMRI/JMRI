// JMRIdemo.java

package apps.JmriDemo;

import apps.Apps;
import java.text.MessageFormat;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import jmri.util.JmriJFrame;
import jmri.util.swing.WindowInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JMRI demo program.
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
public class JMRIdemo extends Apps {

    JMRIdemo(JFrame p) {
        super(p);
    }

    protected String line1() {
        return MessageFormat.format(rb.getString("JmriDemoVersionCredit"),
                                new Object[]{jmri.Version.name()});
    }

    /**
     * Adds the development menu to the default main menu bar.
     *
     * @param menuBar
     * @param wi
     */
    @Override
    protected void createMenus(JMenuBar menuBar, WindowInterface wi) {
        super.createMenus(menuBar, wi);
        developmentMenu(menuBar, wi);
        menuBar.add(new jmri.jmris.ServerMenu());
    }

    /**
     * Show all systems in the menu bar.
     */
    protected void systemsMenu(JMenuBar menuBar, JFrame frame) {
        menuBar.add(new jmri.jmrix.SystemsMenu());
    }

    // Main entry point
    public static void main(String args[]) {

        // show splash screen early
        splash(true);

        Apps.startupInfo("JMRIdemo");

        setConfigFilename("JmriDemoConfig2.xml", args);
        JmriJFrame f = new JmriJFrame("JmriDemo");
        createFrame(new JMRIdemo(f), f);

        log.debug("main initialization done");
        splash(false);
    }

    static Logger log = LoggerFactory.getLogger(JMRIdemo.class.getName());
}


