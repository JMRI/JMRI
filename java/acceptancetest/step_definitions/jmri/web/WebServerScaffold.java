package jmri.web;

import cucumber.api.java8.En;
import jmri.web.server.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.fail;

/**
 * Cucumber helper to handle starting and stopping the web server during web
 * tests.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class WebServerScaffold implements En {

    private WebServer server = null;
    String[] tags = {"@webtest"};

    private final static Logger log = LoggerFactory.getLogger(WebServerScaffold.class);

    public WebServerScaffold(jmri.InstanceManager instance) {

        Before(tags, () -> {
            jmri.util.JUnitUtil.resetProfileManager();
            instance.setDefault(jmri.web.server.WebServerPreferences.class, new jmri.web.server.WebServerPreferences());
            jmri.util.JUnitUtil.initConfigureManager();
            jmri.util.JUnitUtil.initInternalTurnoutManager();
            jmri.util.JUnitUtil.initInternalLightManager();
            jmri.util.JUnitUtil.initInternalSensorManager();
            jmri.util.JUnitUtil.initMemoryManager();
            jmri.util.JUnitUtil.initShutDownManager();
            jmri.util.JUnitUtil.initConnectionConfigManager();
            jmri.util.JUnitUtil.initDebugPowerManager();
            server = new WebServer(); // a webserver using default preferences.
            server.start();
            jmri.util.JUnitUtil.waitFor(() -> {
                return server.isStarted();
            }, "Server Failed to Start in time");
            jmri.util.JUnitOperationsUtil.setupOperationsTests();
        });

        After(tags, () -> {
            try {
                try {
                    server.stop();
                    jmri.util.JUnitUtil.waitFor(() -> {
                        return server.isStopped();
                    }, "Server failed to Stop in time");
                } catch (java.lang.NullPointerException npe) {
                    log.debug("NPE shutting down web server", npe);
                } catch (Exception ex) {
                    // Exception is thrown by the stop call above.
                    // if an Exception occurs here, we may want to raise a flag,
                    log.error("Excecption shutting down web server", ex);
                    fail("Exception occured during web server shutdown",ex);
                }
            } catch (java.lang.NullPointerException npe2) {
                log.debug("NPE shutting down web server", npe2);
            }
            jmri.util.JUnitAppender.suppressErrorMessage("Error on WebSocket message:\nConnection has been closed locally");

        });
    }
}
