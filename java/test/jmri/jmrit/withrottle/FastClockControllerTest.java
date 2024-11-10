
package jmri.jmrit.withrottle;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Brett Hoffman Copyright (C) 2018
 * @author Steve Young Copyright (C) 2024
 */
public class FastClockControllerTest {
    
    @Test
    public void testCtor() {
        FastClockController t = new FastClockController();
        assertNotNull( t, "exists" );
        assertTrue(t.verifyCreation());
    }

    @Test
    public void testClockStateSent(){

        Timebase fastClock = InstanceManager.getDefault(Timebase.class);
        fastClock.setRun(false);

        FastClockController t = new FastClockController();
        ControllerInterfaceScaffold scaf = new ControllerInterfaceScaffold();
        t.addControllerListener(scaf);
        t.sendFastTimeAndRate();

        String last = scaf.getLastPacket();
        assertNotNull(last);

        // typical output PFT1726617192<;>0.0
        String firstThreeChars = last.substring(0, Math.min(last.length(), 3));
        assertEquals("PFT", firstThreeChars);
        String lastThreeChars = last.substring(Math.max(0, last.length() - 3));
        assertEquals("0.0", lastThreeChars);

        assertDoesNotThrow( () -> fastClock.setRate(2));
        fastClock.setRun(true);
        last = scaf.getLastPacket();
        assertNotNull(last);
        lastThreeChars = last.substring(Math.max(0, last.length() - 3));
        assertEquals("2.0", lastThreeChars);

        fastClock.setRun(false);
        last = scaf.getLastPacket();
        assertNotNull(last);
        lastThreeChars = last.substring(Math.max(0, last.length() - 3));
        assertEquals("0.0", lastThreeChars);

        fastClock.dispose();
        t.deregister();

    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
