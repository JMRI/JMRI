package jmri.jmrix.can.cbus;

import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusDccProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                programmer.getMode());        
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                programmer2.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                ((CbusDccProgrammer)programmer).getBestMode());        
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                ((CbusDccProgrammer)programmer2).getBestMode());        
    }

/*
    @Test
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
    
    protected Programmer programmer2;
    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        programmer = new CbusDccProgrammer(tcis);
        programmer2 = new CbusDccProgrammer(tcis);
    }

    @Override
    @AfterEach
    public void tearDown() {
        programmer2 = null;
        programmer = null;
        tcis.terminateThreads();
        tcis = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusDccProgrammerTest.class);

}
