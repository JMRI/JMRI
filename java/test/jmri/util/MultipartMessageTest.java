package jmri.util;

import org.junit.*;
import jmri.web.server.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class MultipartMessageTest {

    private WebServer server = null;

    @Test
    public void testCTor() throws java.io.IOException {
        MultipartMessage t = new MultipartMessage("http://localhost:12080","UTF-8");
        Assert.assertNotNull("exists",t);
        t.finish(); // make sure the port closes.
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        // we need a web server to test this, so start the JMRI webserver here
        // and clean it up in teardown.
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDebugPowerManager();
        server = new WebServer(); // a webserver using default preferences.
        server.start();
        jmri.util.JUnitUtil.waitFor(() -> {
            return server.isStarted();
        }, "Server Failed to Start in time");
        jmri.util.JUnitOperationsUtil.setupOperationsTests();
    }

    @After
    public void tearDown() {
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
                Assert.fail("Exception occured during web server shutdown:" + ex);
            }
        } catch (java.lang.NullPointerException npe2) {
            log.debug("NPE shutting down web server", npe2);
            //Assert.fail("Null Pointer Exception occured during teardown:" + npe2);
        }
        JUnitUtil.resetZeroConfServiceManager();
        jmri.util.JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(MultipartMessageTest.class.getName());

}
