package jmri.jmrix.can.cbus;

import jmri.ProgListenerScaffold;
import jmri.ProgrammingMode;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TestTrafficController;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusProgrammer class.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class CbusProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {
        
    private TrafficControllerScaffold tc = null;
    private CbusProgrammer p = null; 
    private ProgListenerScaffold testListener;

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", CbusProgrammer.CBUSNODEVARMODE,
                programmer.getMode());        
    }

    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", CbusProgrammer.CBUSNODEVARMODE,
                ((CbusProgrammer)programmer).getBestMode());        
    }


    @Test(expected=java.lang.IllegalArgumentException.class)
    @Override
    public void testSetGetMode() {
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                programmer.getMode());        
    }

    @Test
    public void testWriteSequence() throws jmri.ProgrammerException {
        p.writeCV("4", 5, testListener);

        Assert.assertEquals("listeners", 0, tc.numListeners());
        Assert.assertEquals("sent count", 1, tc.outbound.size());
        Assert.assertEquals("content 1", "[78] 96 00 03 04 05",
                tc.outbound.get(0).toString());

        // no reply from CAN and listener replies immediately,
        // contrast read test below
        Assert.assertTrue("listener invoked", testListener.getRcvdInvoked()>0);
        Assert.assertEquals("status", 0, testListener.getRcvdStatus());
    }

    @Test
    public void testReadSequence() throws jmri.ProgrammerException {
        p.readCV("4", testListener);

        Assert.assertEquals("listeners", 0, tc.numListeners());
        Assert.assertEquals("sent count", 1, tc.outbound.size());
        Assert.assertEquals("content 1", "[78] 71 00 03 04",
                tc.outbound.get(0).toString());
        Assert.assertTrue("listener not invoked", testListener.getRcvdInvoked()==0);

        // pretend reply from CAN
        int[] frame = new int[]{0x97, 0, 3, 5};
        CanReply f = new CanReply(frame);
        p.reply(f);

        Assert.assertTrue("listener invoked", testListener.getRcvdInvoked()>0);
        Assert.assertEquals("status", 0, testListener.getRcvdStatus());
        Assert.assertEquals("value", 5, testListener.getRcvdValue());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new TrafficControllerScaffold();
        p = new CbusProgrammer(3, tc);
        programmer = p;
        testListener = new ProgListenerScaffold();
    }

    @Override
    @After
    public void tearDown() {
        programmer = p = null;
        tc = null;
	    testListener = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    	JUnitUtil.tearDown();
    }

}
