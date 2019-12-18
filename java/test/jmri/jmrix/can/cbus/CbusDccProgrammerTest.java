package jmri.jmrix.can.cbus;

import jmri.ProgListenerScaffold;
import jmri.ProgrammingMode;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class CbusDccProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                programmer.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.PAGEMODE,
                ((CbusDccProgrammer)programmer).getBestMode());        
    }

/*
    @Test(expected=java.lang.IllegalArgumentException.class)
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

*/

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        programmer = new CbusDccProgrammer(new TrafficControllerScaffold());
    }

    @Override
    @After
    public void tearDown() {
        programmer = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDccProgrammerTest.class);

}
