package jmri.jmrix.can.cbus;

import jmri.jmrix.AbstractPowerManagerTestBase;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.PowerManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (c) 2019
 * @author Andrew Crosland Copyright (c) 2021
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
        fail("Should not have been called");
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
        assertEquals(PowerManager.OFF, p.getPower(),"set off");

        r = new CanReply( new int[]{CbusConstants.CBUS_TON},0x12);
        r.setExtended(true);

        pwr.reply(r);
        assertEquals(PowerManager.OFF, p.getPower(),"still off");

        r.setExtended(false);
        r.setRtr(true);
        pwr.reply(r);
        assertEquals(PowerManager.OFF, p.getPower(),"still off");

        r.setRtr(false);
        pwr.reply(r);
        assertEquals(PowerManager.ON, p.getPower(),"on");
        assertEquals(0, controller.outbound.size());

    }

    @Test
    public void checkArstBehaviour () throws jmri.JmriException {

        // Test effect of ARST on power state
        CanReply r = new CanReply( new int[]{CbusConstants.CBUS_TOF},0x12);
        pwr.reply(r);
        assertEquals(PowerManager.OFF, p.getPower(),"set off before ARST");

        r = new CanReply( new int[]{CbusConstants.CBUS_ARST},0x12);
        pwr.reply(r);
        assertEquals(PowerManager.ON, p.getPower(),"on after ARST");

        // Change from default behaviour
        memo.setPowerOnArst(false);

        r = new CanReply( new int[]{CbusConstants.CBUS_TOF},0x12);
        pwr.reply(r);
        assertEquals(PowerManager.OFF, p.getPower(),"set off before ARST");

        r = new CanReply( new int[]{CbusConstants.CBUS_ARST},0x12);
        pwr.reply(r);
        assertEquals(PowerManager.OFF, p.getPower(),"still off after ARST");

    }

    @Test
    public void checkName() {
        assertNotNull(pwr.getUserName());
    }

    private CanSystemConnectionMemo memo;
    private CbusPowerManager pwr;
    private TrafficControllerScaffold controller;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        controller = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(controller);
        pwr = new CbusPowerManager(memo);
        p = pwr;

    }

    @AfterEach
    public void tearDown() {

        pwr.dispose();

        memo.dispose();
        controller.terminateThreads();
        pwr = null;
        memo = null;
        controller = null;

        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusPowerManagerTest.class);

}
