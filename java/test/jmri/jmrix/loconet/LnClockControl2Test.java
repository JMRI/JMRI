package jmri.jmrix.loconet;

import java.util.Date;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for LnClockControlTest class.
 *
 * @author Bob Jacobsen (c) 2025
 **/
public class LnClockControl2Test {

    private LnClockControl ldt;
    private Date time;

    private LocoNetInterfaceScaffold lnis;
    private LocoNetSystemConnectionMemo memo;
    private SlotManager slotmanager;

    @Test
    public void testCheckStartValues(){
        assertEquals(1.0, ldt.getRate(), "initial rate");
        assertEquals("Mon Dec 22 00:00:00 CET 2025", time.toString());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();

        // prepare an interface
        lnis = new LocoNetInterfaceScaffold(memo);
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);

        ldt = new LnClockControl(memo);
        time = ldt.getTime();
    }

    @AfterEach
    public void tearDown(){
        memo.dispose();
        lnis = null;

        JUnitUtil.tearDown();
    }

}
