package jmri.jmrix.loconet;

import java.util.Date;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    }

    @Test
    public void testConfigureHardware() throws jmri.JmriException {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);

        // allow actual write
        jmri.InstanceManager.getDefault(jmri.Timebase.class).setSynchronize(true, false);

        // set power manager to ON
        c.getPowerManager().setPower(jmri.PowerManager.ON);
        c.getPowerManager().message(lnis.outbound.get(0));
        lnis.outbound.removeAllElements();

        LnClockControl t = new LnClockControl(c);
        
        // configure, hence write
        Date testDate = new Date(2018, 12, 1);  // deprecated, but OK for test
        t.initializeHardwareClock(1.0, testDate, false);
        
        // expect two messages
        Assert.assertEquals("sent", 2, lnis.outbound.size());
        Assert.assertEquals("message 1", "EF 0E 7B 01 7B 78 43 07 68 01 00 00 00 00", lnis.outbound.get(0).toString());
        Assert.assertEquals("message 2", "BB 7B 00 00", lnis.outbound.get(1).toString());     
        
        c.dispose();
    }
    
    @Test
    public void testPowerBit() throws jmri.JmriException {
        // a brute-force approach to testing that the power bit follows
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);

        // allow actual write
        jmri.InstanceManager.getDefault(jmri.Timebase.class).setSynchronize(true, false);

        // set power manager to OFF
        c.getPowerManager().setPower(jmri.PowerManager.OFF);
        c.getPowerManager().message(lnis.outbound.get(0));
        lnis.outbound.removeAllElements();
                
        LnClockControl t = new LnClockControl(c);
        
        // configure, hence write
        Date testDate = new Date(2018, 12, 1);  // deprecated, but OK for test
        t.initializeHardwareClock(1.0, testDate, false);
        
        // expect two messages
        Assert.assertEquals("sent", 2, lnis.outbound.size());
        Assert.assertEquals("message 1", "EF 0E 7B 01 7B 78 43 06 68 01 00 00 00 00", lnis.outbound.get(0).toString());
        Assert.assertEquals("message 2", "BB 7B 00 00", lnis.outbound.get(1).toString());     
        
        c.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnClockControlTest.class);

}
