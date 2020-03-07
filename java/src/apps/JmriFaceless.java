package apps;

/**
 * Application for running JMRI server functions without a graphical interface.
 * Goal is to run on very light hardware, such as a Raspberry Pi, without
 * requiring the overhead associated with display functions. Needs an existing
 * JMRI configuration file passed as parm to program, or it will use the default
 * of JmriFacelessConfig3.xml Copied from apps.gui3.demo3.Demo3, then removed
 * all ui-related elements NOTE: JMRI "server" functions based on ui components
 * (such as JmriJFrames) may need to be modified to check isHeadless() and
 * adjust their behavior as needed.
 * <br>
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
 * @author Steve Todd Copyright 2012
 */
public class JmriFaceless extends apps.AppsBase {

    //private static final Logger log = LoggerFactory.getLogger(JmriFaceless.class);
    public JmriFaceless(String[] args) {
        super("JmriFaceless", "JmriFacelessConfig3.xml", args);
    }

    @Override
    public void start() {
        // Once the configServlet is usable, require the web server
        // WebServerManager.getWebServer().start();
        super.start();
    }

    // Main entry point
    public static void main(String args[]) {
        System.setProperty("java.awt.headless", "true");
        JmriFaceless app = new JmriFaceless(args);
        app.start();
    }

}
