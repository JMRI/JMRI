package jmri.util;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.*;

import jmri.web.server.WebServer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class MultipartMessageTest {

    private WebServer server = null;

    @Test
    public void testCTor() throws java.io.IOException, java.net.URISyntaxException {
        MultipartMessage t = new MultipartMessage("http://localhost:12080",StandardCharsets.UTF_8.name());
        assertNotNull( t, "exists");
        t.finish(); // make sure the port closes.
    }

    @BeforeEach
    public void setUp() {
        // we need a web server to test this, so start the JMRI webserver here
        // and clean it up in teardown.
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDebugPowerManager();
        server = new WebServer(); // a webserver using default preferences.
        server.start();
        JUnitUtil.waitFor(() -> {
            return server.isStarted();
        }, "Server Failed to Start in time");
        jmri.util.JUnitOperationsUtil.setupOperationsTests();
    }

    @AfterEach
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "DCN_NULLPOINTER_EXCEPTION",
        justification = "custom NPE handling ")
    public void tearDown() {
        assertNotNull(server);
        try {
            try {
                server.stop();
                JUnitUtil.waitFor(() -> {
                    return server.isStopped();
                }, "Server failed to Stop in time");
            } catch (NullPointerException npe) {
                log.debug("NPE shutting down web server", npe);
            } catch (Exception ex) {
                // Exception is thrown by the stop call above.
                // if an Exception occurs here, we may want to raise a flag,
                log.error("Excecption shutting down web server", ex);
                fail("Exception occured during web server shutdown:" + ex);
            }
        } catch (NullPointerException npe2) {
            log.debug("NPE shutting down web server", npe2);
            //Assert.fail("Null Pointer Exception occured during teardown:" + npe2);
        }
        assertTrue(JUnitUtil.resetZeroConfServiceManager());
        JUnitUtil.tearDown();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MultipartMessageTest.class.getName());

}
