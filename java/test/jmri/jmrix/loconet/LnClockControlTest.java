package jmri.jmrix.loconet;

import java.util.Date;
import jmri.jmrix.loconet.LnCommandStationType.CommandStationFracType;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnClockControlTest {

    @Test
    public void testCtorOneArg() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);

        LnClockControl t = new LnClockControl(c);
        Assert.assertNotNull("exists",t);

        c.dispose();
    }

    @Test
    public void testCtorTwoArg() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
 
        LnClockControl t = new LnClockControl(slotmanager, lnis, null);
 
        Assert.assertNotNull("exists",t);
        slotmanager.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnClockControlTest.class);

}
