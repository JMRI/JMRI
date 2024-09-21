package jmri.jmrit.withrottle;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of TurnoutController
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2024
 */
public class TurnoutControllerTest {

    @Test
    public void testTurnoutControllerCtor() {
        TurnoutController t = new TurnoutController();
        assertNotNull( t, "exists" );
        assertTrue(t.verifyCreation());
    }

    @Test
    public void testSendTitles(){
        TurnoutController t = new TurnoutController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);
        t.sendList();

        t.sendTitles();

        String last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals(
            "PTT]\\[Turnouts}|{Turnout]\\[Closed}|{2]\\[Thrown}|{4]\\[Unknown}|{1]\\[Inconsistent}|{8",
            last);
        t.deregister();
    }

    @Test
    public void testSendList(){

        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("1").setUserName("MyName1");
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("2").setUserName("MyName2");

        TurnoutController t = new TurnoutController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);
        t.sendList();

        String last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PTL]\\[IT1}|{MyName1}|{1]\\[IT2}|{MyName2}|{1", last);
        t.deregister();
    }

    @Test
    public void testTurnoutStateSent(){

        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("1").setUserName("MyName1");
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("2").setUserName("MyName2");
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("1");

        TurnoutController t = new TurnoutController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);
        t.sendList();

        turnout.setCommandedState(Turnout.CLOSED);

        String last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PTA2IT1", last);

        turnout.setCommandedState(Turnout.THROWN);
        last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PTA4IT1", last);

        turnout.setCommandedState(Turnout.INCONSISTENT);
        last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PTA8IT1", last);

        turnout.setCommandedState(Turnout.UNKNOWN);
        last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PTA1IT1", last);

        t.deregister();

    }

    @Test
    public void testTurnoutStateReceived(){

        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("1").setUserName("MyName1");
        InstanceManager.getDefault(TurnoutManager.class).provideTurnout("2").setUserName("MyName2");
        Turnout turnout = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("1");
        turnout.setCommandedState(Turnout.THROWN);

        TurnoutController t = new TurnoutController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);
        t.sendList();

        // PT stripped by DeviceServer
        t.handleMessage("ACIT1", null);
        JUnitUtil.waitFor( () -> turnout.getCommandedState()==Turnout.CLOSED, "turnout went closed");

        t.handleMessage("ATIT1", null);
        JUnitUtil.waitFor( () -> turnout.getCommandedState()==Turnout.THROWN, "turnout went thrown");

        t.deregister();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
