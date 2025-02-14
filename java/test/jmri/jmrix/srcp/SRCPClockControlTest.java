package jmri.jmrix.srcp;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * SRCPClockControlTest.java
 *
 * Test for the jmri.jmrix.srcp.SRCPClockControl class
 *
 * @author Bob Jacobsen
 */
public class SRCPClockControlTest {

    private Timebase tb;
    private SRCPClockControl t;
    private SRCPBusConnectionMemo sm;
    private SRCPTrafficControllerImpl tc;

    @Test
    public void testCtor() {
        Assertions.assertNotNull(t);
    }

    @Test
    public void testSendATime() {

        Assertions.assertNotNull(t);
        InstanceManager.setDefault(ClockControl.class, t);
        // Assertions.assertEquals( 0, tc.getSentMessages().size() );

        tb = jmri.InstanceManager.getDefault(Timebase.class);
        tb.setRun(false);

        tb.setInternalMaster(true, false);
        tb.setSynchronize(true, true);
        
        LocalDateTime specificDate = LocalDateTime.of(2020, 04, 24, 17, 57, 41);
        tb.setTime(Date.from( specificDate.atZone( ZoneId.systemDefault()).toInstant())); // a Friday
        Assertions.assertEquals( 2, tc.getSentMessages().size() );

        SRCPMessage m = tc.getSentMessages().get(1);
        Assertions.assertEquals("SET 1 TIME 2020115 17 57 41", m.toString());

        sm.getTrafficController().terminateThreads();
        sm.dispose();

    }

    private static class SRCPTrafficControllerImpl extends SRCPTrafficController {

        private final java.util.List<SRCPMessage> lastSent = new java.util.ArrayList<>();

        @Override
        public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            lastSent.add(m);
        }

        java.util.List<SRCPMessage> getSentMessages(){
            return Collections.unmodifiableList(lastSent);
        }

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SRCPTrafficControllerImpl();
        sm = new SRCPBusConnectionMemo( tc , "A", 1);
        t = new SRCPClockControl(sm);
    }

    @AfterEach
    public void tearDown() {
        tb = jmri.InstanceManager.getNullableDefault(Timebase.class);
        if (tb !=null){
            tb.dispose();
            tb = null;
        }
        tc.terminateThreads();
        tc = null;
        sm.dispose();
        sm = null;

        JUnitUtil.tearDown();
    }
}
