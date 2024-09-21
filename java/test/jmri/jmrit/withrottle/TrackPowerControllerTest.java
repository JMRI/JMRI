package jmri.jmrit.withrottle;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test simple functioning of TrackPowerController
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2024
 */
public class TrackPowerControllerTest {

    @Test
    public void testCtor() {
        TrackPowerController t = new TrackPowerController();
        assertNotNull( t, "exists" );
        assertTrue(t.verifyCreation());
    }

    @Test
    public void testPowerStateSent(){

        PowerManager pm = InstanceManager.getDefault(PowerManager.class);
        assertDoesNotThrow( () -> pm.setPower(PowerManager.UNKNOWN));

        TrackPowerController t = new TrackPowerController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);
        t.sendCurrentState();

        String last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PPA2", last);
        assertDoesNotThrow( () -> pm.setPower(PowerManager.OFF));
        last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PPA0", last);

        assertDoesNotThrow( () -> pm.setPower(PowerManager.ON));
        last = scaf.getLastPacket();
        assertNotNull(last);
        assertEquals("PPA1", last);

        t.deregister();

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDebugPowerManager();

    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();

    }
}
