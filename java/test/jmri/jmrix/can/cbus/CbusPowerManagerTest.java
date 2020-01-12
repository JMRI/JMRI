package jmri.jmrix.can.cbus;

import jmri.jmrix.AbstractPowerManagerTestBase;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.PowerManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 */
public class CbusPowerManagerTest extends AbstractPowerManagerTestBase {


    /**
     * service routines to simulate receiving on from command station
     */
    @Override
    protected void hearOn() {
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_TON},0x12);
        pwr.reply(r);
    }

    /**
     * service routines to simulate receiving off from command station
     */
    @Override
    protected void hearOff() {
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_TOF},0x12);
        pwr.reply(r);
    }
    
    @Override
    protected void sendOnReply() {
        hearOn();
    }    

    @Override
    protected void sendOffReply() {
        hearOff();
    }
    
    @Override
    protected boolean outboundIdleOK(int index) {
        return false;
    }

    @Override
    protected boolean outboundOnOK(int index) {
        return CbusConstants.CBUS_RTON == controller.outbound.elementAt(index).getOpCode();
    }

    @Override
    protected boolean outboundOffOK(int index) {
        return CbusConstants.CBUS_RTOF == controller.outbound.elementAt(index).getOpCode();
    }

    @Override
    protected int numListeners() {
        return controller.numListeners();
    }

    @Override
    protected int outboundSize() {
        return controller.outbound.size();
    }

    @Override
    protected void hearIdle() {
    }

    @Override
    protected void sendIdleReply() {
        Assert.fail("Should not have been called");
    }
    
    @Test
    public void checkCanMessage() {
        // unused but needs to be there for CanListener
        jmri.jmrix.can.CanMessage m = new jmri.jmrix.can.CanMessage(new int[]{CbusConstants.CBUS_TON},0x12);
        pwr.message(m);
        
    }
    
    
    @Test
    public void checkCanReplyExtendedRtr () throws jmri.JmriException {
        
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_TOF},0x12);
        pwr.reply(r);
        Assert.assertEquals("set off", PowerManager.OFF, p.getPower());
        
        r = new CanReply( new int[]{CbusConstants.CBUS_TON},0x12);
        r.setExtended(true);
        
        pwr.reply(r);
        Assert.assertEquals("still off", PowerManager.OFF, p.getPower());
        
        r.setExtended(false);
        r.setRtr(true);
        pwr.reply(r);
        Assert.assertEquals("still off", PowerManager.OFF, p.getPower());
        
        r.setRtr(false);
        pwr.reply(r);
        Assert.assertEquals("on", PowerManager.ON, p.getPower());
        
    }
    
    CanSystemConnectionMemo memo;
    CbusPowerManager pwr;
    TrafficControllerScaffold controller;
    
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        controller = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(controller);
        p = pwr = new CbusPowerManager(memo);        
        
    }

    @After
    public void tearDown() {
        
        try {
            pwr.dispose();
        } catch (jmri.JmriException ex) {}
        
        memo.dispose();
        pwr = null;
        memo = null;
        controller = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusPowerManagerTest.class);

}
