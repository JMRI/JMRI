package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of FacelessServer
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class FacelessServerTest {

    private FacelessServer server = null;

    @Test
    public void testFacelessServerCtor() {
        Assertions.assertNotNull(server, "exists" );
    }

    @Test
    public void testGetDeviceList() {
        Assertions.assertNotNull(server);
        Assertions.assertNotNull( server.getDeviceList(), "exists" );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        server = new FacelessServer(){
            @Override
            public void listen(){
            }
        };
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(server);
        server.disableServer();
        JUnitUtil.waitFor( () -> { return !server.isListen; },"Server stops listening flag");

        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
