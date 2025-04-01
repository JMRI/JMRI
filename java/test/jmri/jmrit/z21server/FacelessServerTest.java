package jmri.jmrit.z21server;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of FacelessServer
 *
 * @author Eckart Meyer (C) 2025
 */
public class FacelessServerTest {

    private FacelessServer server = null;

    @Test
    public void testFacelessServerCtor() {
        Assertions.assertNotNull(server, "exists" );
    }

//    @Test
//    public void testGetDeviceList() {
//        Assertions.assertNotNull(server);
//        Assertions.assertNotNull( server.getDeviceList(), "exists" );
//    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        server = FacelessServer.getInstance();
    }

    @AfterEach
    public void tearDown() throws Exception {
        Assertions.assertNotNull(server);
        server.stop();
        //JUnitUtil.waitFor( () -> { return !server.isListen; },"Server stops listening flag");

        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
