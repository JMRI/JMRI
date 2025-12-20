package jmri.jmrit.withrottle;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of RouteController
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2024
 */
public class RouteControllerTest {

    @Test
    public void testCtor() {
        RouteController t = new RouteController();
        assertNotNull( t, "exists" );
        assertTrue(t.verifyCreation());
    }

    @Test
    public void testSendTitles(){
        RouteController t = new RouteController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);
        t.sendList();

        t.sendTitles();

        String last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals(
            "PRT]\\[Routes}|{Route]\\[Active}|{2]\\[Inactive}|{4]\\[Unknown}|{0]\\[Inconsistent}|{8]\\[Unknown}|{"
            , last);
        t.deregister();
    }

    @Test
    public void testSendRouteList(){

        Route route = InstanceManager.getDefault(RouteManager.class).provideRoute("IO1", "MyRoute1");
        Assertions.assertNotNull(route);

        RouteController t = new RouteController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);
        t.sendList();

        String last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PRL]\\[IO1}|{MyRoute1}|{", last);

        t.deregister();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initRouteManager();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
