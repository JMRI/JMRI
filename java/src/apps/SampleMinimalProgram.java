package apps;

import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.util.prefs.JmriPreferencesActionFactory;
import jmri.web.server.WebServer;
import jmri.web.server.WebServerPreferences;

import apps.util.Log4JUtil;

/**
 * A simple example of a "Faceless" (no gui) application
 *
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
 * @author Bob Jacobsen Copyright 2003, 2005, 2007, 2010
 */
public class SampleMinimalProgram {

    static String name = "Faceless App";

    // Main entry point
    public static void main(String args[]) {

        initLog4J();
        log.info("Startup: {}", Log4JUtil.startupInfo(name));

        new SampleMinimalProgram(args);   // start the application class itself

        log.debug("main initialization done");

        // You could put your own code here,
        // for example.  The layout connection
        // is working at this point.
    }

    /**
     * Static method to return a first logging statement. Used for logging
     * startup, etc.
     *
     * @param program the name of the program
     * @return the logging statement including JMRI and Java versions
     */
    static public String startupInfo(String program) {
        return (program + " version " + jmri.Version.name()
                + " starts under Java " + System.getProperty("java.version", "<unknown>"));
    }

    /**
     * Static method to get Log4J working before the rest of JMRI starts up. In
     * a non-minimal program, invoke jmri.util.Log4JUtil.initLogging
     */
    static protected void initLog4J() {
        // initialize log4j2 - from logging configuration file (lcf) only
        // if can find it!
        String configFile = "default_lcf.xml";
        apps.util.Log4JUtil.initLogging(configFile);
        // install default exception handler
        Thread.setDefaultUncaughtExceptionHandler(new jmri.util.exceptionhandler.UncaughtExceptionHandler());
    }

    /**
     * Constructor starts the JMRI application running, and then returns.
     *
     * @param args command line arguments set at application launch
     */
    public SampleMinimalProgram(String[] args) {

        // Load from preference file, by default the DecoderPro
        // one so you can use DecoderPro to load the preferences values.
        //    setConfigFilename("DecoderProConfig2.xml", args);
        //    loadFile();
        // load directly via code
        codeConfig(args);

        // and here we're up and running!
    }

    protected void codeConfig(String[] args) {
        jmri.jmrix.SerialPortAdapter adapter = new jmri.jmrix.lenz.li100.LI100Adapter();

        String portName = "/dev/cu.Bluetooth-PDA-Sync";
        String baudRate = "9600";
        //String option1Setting = null;
        //String option2Setting = null;

        adapter.setPort(portName);
        adapter.configureBaudRate(baudRate);
        //if (option1Setting !=null) adapter.configureOption1(option1Setting);
        //if (option2Setting !=null) adapter.configureOption2(option2Setting);

        adapter.openPort(portName, "JMRI app");
        adapter.configure();

        // install a Preferences Action Factory.
        InstanceManager.store(new AppsPreferencesActionFactory(), JmriPreferencesActionFactory.class);

        ConfigureManager cm = new AppsConfigurationManager();

        // not setting preference file location!
        InstanceManager.setDefault(ConfigureManager.class, cm);
        // needs an error handler that doesn't invoke swing; send to log4j?

        // start web server
        final int port = 12080;
        InstanceManager.getDefault(WebServerPreferences.class).setPort(port);
        try {
            WebServer.getDefault().start();
        } catch (Exception ex) {
            log.error("Unable to start web server.", ex);
        }

        log.info("Up!");
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SampleMinimalProgram.class);

}
