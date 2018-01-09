package jmri.jmrix.loconet;

import jmri.ProgListenerScaffold;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import jmri.jmrix.loconet.SlotManager;

import java.util.List;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 *
 * @author given
 */
public class csOpSwAccessTest {

    LocoNetInterfaceScaffold lnis;
    SlotManager sm;
    LocoNetSystemConnectionMemo memo;
    ProgListenerScaffold pl;

    public csOpSwAccessTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        // The minimal setup for log4J
        apps.tests.Log4JFixture.setUp();
        lnis = new LocoNetInterfaceScaffold();
        sm = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, sm);
        pl = new ProgListenerScaffold();
        sm.setSystemConnectionMemo(memo);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testCommandStationRead1() throws ProgrammerException {

        csOpSwAccess csosa = new csOpSwAccess(memo,pl);

        Assert.assertNotNull("checkMemo", memo);
        Assert.assertNotNull("checkTc", memo.getLnTrafficController());

        // attempt a command station opsw access
        csosa.readCsOpSw("csOpSw.01", pl);

        // should have sent a message
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // should have written and not returned

        Assert.assertEquals("sent byte 0", 0xBB, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(0).getElement(2) & 0xFF);

        int testVal = 0;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        csosa.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        // command station reply
        m = new LocoNetMessage(new int[] {LnConstants.OPC_SL_RD_DATA,
            0x0E, 0x7F, 0x10, 0x00, 0x30, 0x02,
            0x07, 0x01, 0x08, 0x00, 0x1B, 0x69, 0x77});
        csosa.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.02", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.03", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.04", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.05", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another (out-of-order) command station opsw access
        csosa.readCsOpSw("csOpSw.07", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 6, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another (out-of-order) command station opsw access
        csosa.readCsOpSw("csOpSw.06", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 7, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.08", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming error reply ", 8, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.17", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 9, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.18", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 10, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.19", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 11, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.20", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 12, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.21", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 13, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.22", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 14, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.23", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 15, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.24", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 16, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.25", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 17, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.26", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 18, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.27", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 19, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.28", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 20, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.29", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 21, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.30", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 22, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.31", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 23, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.32", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 24, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());
     }

    @Test
    public void testCommandStationReadTimeout() throws ProgrammerException {
        csOpSwAccess csosa = new csOpSwAccess(memo,pl);
        csosa.readCsOpSw("csOpSw.14", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // No reply message, wait for timeout
        jmri.util.JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"programming reply not received");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status Not OK", jmri.ProgListener.FailedTimeout, pl.getRcvdStatus());
        Assert.assertTrue("Correct thread", pl.wasRightThread());
     }

    @Test
    public void testCommandStationRead2() throws ProgrammerException {
        csOpSwAccess csosa = new csOpSwAccess(memo,pl);

        // attempt a command station opsw access
        csosa.readCsOpSw("csOpSw.01", pl);

        // should have sent a message
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // should have written and not returned

        Assert.assertEquals("sent byte 0", 0xBB, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(0).getElement(2) & 0xFF);

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        csosa.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        // command station reply
        m = new LocoNetMessage(new int[] {LnConstants.OPC_SL_RD_DATA,
            0x0E, 0x7F, 0x7d, 0x6e, 0x5a, 0x4d,
            0x07, 0x3b, 0x2e, 0x1e, 0x0f, 0x69, 0x77});
        csosa.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.02", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.03", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.04", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.05", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.06", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 6, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.07", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 7, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.08", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 8, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.09", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 9, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.10", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 10, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.11", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 11, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.12", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 12, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.13", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 13, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.14", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 14, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.15", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 15, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.16", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 16, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.17", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 17, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.18", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 18, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.19", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 19, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.20", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 20, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.21", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 21, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.22", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 22, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.23", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 23, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.24", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 24, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.25", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 25, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.26", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 26, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.27", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 27, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.28", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 28, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.29", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 29, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.30", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 30, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.31", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 31, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.32", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 32, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.33", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 33, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.34", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 34, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.35", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 35, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.36", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 36, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.37", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 37, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.38", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 38, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.39", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 39, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.40", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 40, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.41", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 41, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.42", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 42, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.43", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 43, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.44", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 44, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.45", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 45, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.46", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 46, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.47", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 47, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.48", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 48, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.49", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 49, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.50", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 50, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.51", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 51, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.52", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 52, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.53", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 53, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.54", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 54, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.55", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 55, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.56", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 56, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.57", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 57, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.58", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 58, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.59", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 59, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.60", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 60, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.61", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 61, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.62", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 62, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.63", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 63, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        csosa.readCsOpSw("csOpSw.64", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 64, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt an out-of-range command station opsw access
        csosa.readCsOpSw("csOpSw.65", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status: Not Implemented", 8, pl.getRcvdStatus());
        Assert.assertEquals("should get a programming reply", 65, pl.getRcvdInvoked());

        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access of OpSw 65 account out-of-range for this command station.");

     }

    @Test
    public void testCommandStationReadOutOfBounds1() throws ProgrammerException {
        csOpSwAccess csosa = new csOpSwAccess(memo,pl);

        // attempt a command station opsw access
        csosa.readCsOpSw("csOpSw.01", pl);

        // should have sent a message
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // should have written and not returned

        Assert.assertEquals("sent byte 0", 0xBB, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(0).getElement(2) & 0xFF);

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        csosa.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        // command station reply
        m = new LocoNetMessage(new int[] {LnConstants.OPC_SL_RD_DATA,
            0x0E, 0x7F, 0x7d, 0x6e, 0x5a, 0x4d,
            0x07, 0x3b, 0x2e, 0x1e, 0x0f, 0x69, 0x77});
        csosa.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt an out-of-range command station opsw access
        csosa.readCsOpSw("csOpSw.0", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=2, parts[]=[csOpSw, 0]");

        // attempt an out-of-range command station opsw access
        csosa.readCsOpSw("csOpSw.-1", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=2, parts[]=[csOpSw, -1]");

        // attempt an out-of-range command station opsw access
        csosa.readCsOpSw("cs", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=1, parts[]=[cs]");

     }

    @Test
    public void testCommandStationWriteOutOfBounds1() throws ProgrammerException {
        csOpSwAccess csosa = new csOpSwAccess(memo,pl);

        // attempt an out-of-range command station opsw access
        csosa.writeCsOpSw("csOpSw.0", 1, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=2, parts[]=[csOpSw, 0], val=1");

        // attempt an out-of-range command station opsw access
        csosa.writeCsOpSw("csOpSw.-1", 0, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=2, parts[]=[csOpSw, -1], val=0");

        // attempt an out-of-range command station opsw access
        csosa.writeCsOpSw("cs", 1, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Sequence Error", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=1, parts[]=[cs], val=1");

        // attempt an out-of-range command station opsw access
        csosa.writeCsOpSw("csOpSw.1", -1, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=2, parts[]=[csOpSw, 1], val=-1");

        // attempt an out-of-range command station opsw access
        csosa.writeCsOpSw("csOpSw.1", 2, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=2, parts[]=[csOpSw, 1], val=2");

     }

     @Test
     public void testCmdStnOpSwWrite() throws ProgrammerException {
        csOpSwAccess csosa = new csOpSwAccess(memo,pl);

        csosa.writeCsOpSw("csOpSw.5", 1, pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0", 0xbb, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(0).getElement(2) & 0xFF);

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        csosa.message(m); // propagate the message from

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());
        Assert.assertEquals("received no messages", 0, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xE7, 0x0e, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x69});

        lnis.sendTestMessage(m);
        Assert.assertEquals("sent byte 0", 0xeF, lnis.outbound.get(1).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x0e, lnis.outbound.get(1).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x7f, lnis.outbound.get(1).getElement(2) & 0xFF);
        Assert.assertEquals("sent byte 3", 0x10, lnis.outbound.get(1).getElement(3) & 0xFF);
        Assert.assertEquals("sent byte 4", 0x00, lnis.outbound.get(1).getElement(4) & 0xFF);
        Assert.assertEquals("sent byte 5", 0x00, lnis.outbound.get(1).getElement(5) & 0xFF);
        Assert.assertEquals("sent byte 6", 0x00, lnis.outbound.get(1).getElement(6) & 0xFF);
        Assert.assertEquals("sent byte 7", 0x00, lnis.outbound.get(1).getElement(7) & 0xFF);
        Assert.assertEquals("sent byte 8", 0x00, lnis.outbound.get(1).getElement(8) & 0xFF);
        Assert.assertEquals("sent byte 9", 0x00, lnis.outbound.get(1).getElement(9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(1).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(1).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(1).getElement(12) & 0xFF);

        // No reply message, wait for timeout
        jmri.util.JUnitUtil.waitFor(()->{return lnis.outbound.size() == 2;},"programming reply not received");
        csosa.message(m); // propagate reply to to ops mode programmer

        Assert.assertEquals("two message sent", 2, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());
        Assert.assertEquals("received one messages", 1, lnis.getReceivedMsgCount());

        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("two messages sent", 2, lnis.outbound.size());
        Assert.assertEquals("one programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        csosa.writeCsOpSw("csOpSw.5", 0, pl);

        // should have written
        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());
        Assert.assertEquals("one previous programming reply", 1, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(2).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(2).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(2).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x00, lnis.outbound.get(2).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(2).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(2).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(2).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(2).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(2).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(2).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(2).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(2).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(2).getElement(12) & 0xFF);
        // check echo of sent message has no effect
        m = lnis.outbound.get(2);
        csosa.message(m); // propagate the message from

        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());
        Assert.assertEquals("one previous programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 2, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("another message sent", 3, lnis.outbound.size());
        Assert.assertEquals("two programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write (same as last one)
        csosa.writeCsOpSw("csOpSw.5", 0, pl);

        // should have written
        Assert.assertEquals("four messages sent", 4, lnis.outbound.size());
        Assert.assertEquals("two previous programming replies", 2, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(3).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(3).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(3).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x00, lnis.outbound.get(3).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(3).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(3).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(3).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(3).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(3).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(3).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(3).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(3).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(3).getElement(12) & 0xFF);

        // check echo of sent message has no effect
        m = lnis.outbound.get(3);
        csosa.message(m); // propagate the message from

        Assert.assertEquals("four messages sent", 4, lnis.outbound.size());
        Assert.assertEquals("one previous programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 3, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("another message sent", 4, lnis.outbound.size());
        Assert.assertEquals("three programming replies", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        
        // try another write
        csosa.writeCsOpSw("csOpSw.1", 1, pl);

        // should have written
        Assert.assertEquals("four messages sent", 5, lnis.outbound.size());
        Assert.assertEquals("three previous programming replies", 3, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(4).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(4).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(4).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x01, lnis.outbound.get(4).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(4).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(4).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(4).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(4).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(4).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(4).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(4).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(4).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(4).getElement(12) & 0xFF);

        // check echo of sent message has no effect
        m = lnis.outbound.get(3);
        csosa.message(m); // propagate the message from

        Assert.assertEquals("four messages sent", 5, lnis.outbound.size());
        Assert.assertEquals("three previous programming replies", 3, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 4, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("another message sent", 5, lnis.outbound.size());
        Assert.assertEquals("four programming replies", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        csosa.writeCsOpSw("csOpSw.63", 1, pl);

        // should have written
        Assert.assertEquals("four messages sent", 6, lnis.outbound.size());
        Assert.assertEquals("four previous programming replies", 4, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(5).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(5).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(5).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x01, lnis.outbound.get(5).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(5).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(5).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(5).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(5).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(5).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(5).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(5).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x40, lnis.outbound.get(5).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(5).getElement(12) & 0xFF);

        // check echo of sent message has no effect
        m = lnis.outbound.get(5);
        csosa.message(m); // propagate the message from

        Assert.assertEquals("six messages sent", 6, lnis.outbound.size());
        Assert.assertEquals("four previous programming replies", 4, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 5, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("another message sent", 6, lnis.outbound.size());
        Assert.assertEquals("five programming replies", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        csosa.writeCsOpSw("csOpSw.56", 1, pl);

        // should NOT have written OpSw56 (56 is evenly divisible by 8, so not writable)
        Assert.assertEquals("no additional message sent", 6, lnis.outbound.size());
        Assert.assertEquals("six programming replies", 6, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status bad", 1, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot program OpSw56 account LocoNet encoding limitations.");
     }

@Test
     public void testCmdStnExtendedOpSwWrite() throws ProgrammerException {
        int obIndex=0;
        csOpSwAccess csosa = new csOpSwAccess(memo,pl);

        Assert.assertEquals("Are no outbound messages so far", 0, lnis.transmittedMsgCount);

        Assert.assertEquals("Correct state is idle",csOpSwAccess.cmdStnOpSwStateType.IDLE, csosa.getState());
        csosa.writeCsOpSw("csOpSw.65", 0, pl);

        // should have sent read reqiest
        Assert.assertEquals("Correct state is QUERY",csOpSwAccess.cmdStnOpSwStateType.QUERY, csosa.getState());
        Assert.assertEquals("one message sent", 1, obIndex = lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        obIndex--;
        Assert.assertEquals("sent byte 0", 0xbb, lnis.outbound.get(obIndex).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(obIndex).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(obIndex).getElement(2) & 0xFF);

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        csosa.message(m); // propagate the message from lsis to the csosa

        Assert.assertEquals("still one message sent", 1, obIndex = lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());
        Assert.assertEquals("received no messages", 0, lnis.getReceivedMsgCount());

        // Known-good message in reply (reply to query of slot 0x7f)
        m = new LocoNetMessage(new int[]{0xE7, 0x0e, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x69});

        lnis.sendTestMessage(m);

        Assert.assertEquals("one message sent", 2, obIndex = lnis.outbound.size());

        obIndex--;
        Assert.assertEquals("sent byte 0", 0xbb, lnis.outbound.get(obIndex).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7e, lnis.outbound.get(obIndex).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(obIndex).getElement(2) & 0xFF);
        Assert.assertEquals("Correct state is QUERY_ENHANCED",csOpSwAccess.cmdStnOpSwStateType.QUERY_ENHANCED, csosa.getState());

        // check echo of sent message has no effect
        m = lnis.outbound.get(1);
        csosa.message(m); // echo the message back to the csosa

        // Known-good message in reply (reply to query of slot 0x7e)
        m = new LocoNetMessage(new int[]{0xE7, 0x0e, 0x7e, 0x65, 0x00, 0x00, 0x00, 0x40, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x69});

        lnis.sendTestMessage(m);

        // No reply message, wait for timeout
        jmri.util.JUnitUtil.waitFor(()->{return lnis.outbound.size() == 3;},"programming reply not received");
//        csosa.message(m); // propagate reply

        Assert.assertEquals("three messages sent", 3, obIndex = lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());
        Assert.assertEquals("received two messages", 2, lnis.getReceivedMsgCount());

        obIndex--;
        // check the csosa slot write message for validity
        Assert.assertEquals("opcode of third csosa-transmitted message", 0xEF, lnis.outbound.get(obIndex).getElement(0) & 0xFF);
        Assert.assertEquals("Byte 1 of third csosa-transmitted message", 0x0E, lnis.outbound.get(obIndex).getElement(1) & 0xFF);
        Assert.assertEquals("Byte 2 of third csosa-transmitted message", 0x7E, lnis.outbound.get(obIndex).getElement(2) & 0xFF);
        Assert.assertEquals("Byte 3 of third csosa-transmitted message", 0x64, lnis.outbound.get(obIndex).getElement(3) & 0xFF);
        Assert.assertEquals("Byte 4 of third csosa-transmitted message", 0x00, lnis.outbound.get(obIndex).getElement(4) & 0xFF);
        Assert.assertEquals("Byte 5 of third csosa-transmitted message", 0x00, lnis.outbound.get(obIndex).getElement(5) & 0xFF);
        Assert.assertEquals("Byte 6 of third csosa-transmitted message", 0x00, lnis.outbound.get(obIndex).getElement(6) & 0xFF);
        Assert.assertEquals("Byte 8 of third csosa-transmitted message", 0x00, lnis.outbound.get(obIndex).getElement(8) & 0xFF);
        Assert.assertEquals("Byte 9 of third csosa-transmitted message", 0x00, lnis.outbound.get(obIndex).getElement(9) & 0xFF);
        Assert.assertEquals("Byte 10 of third csosa-transmitted message", 0x00, lnis.outbound.get(obIndex).getElement(10) & 0xFF);
        Assert.assertEquals("Byte 11 of third csosa-transmitted message", 0x00, lnis.outbound.get(obIndex).getElement(11) & 0xFF);
        Assert.assertEquals("Byte 12 of third csosa-transmitted message", 0x00, lnis.outbound.get(obIndex).getElement(12) & 0xFF);

        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());
        Assert.assertEquals("got a programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        csosa.writeCsOpSw("csOpSw.5", 1, pl);

        // should have written
        Assert.assertEquals("four messages sent", 4, obIndex = lnis.outbound.size());
        Assert.assertEquals("one previous programming reply", 1, pl.getRcvdInvoked());

        obIndex--;
        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(obIndex).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(obIndex).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(obIndex).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x10, lnis.outbound.get(obIndex).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(obIndex).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(obIndex).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(obIndex).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(obIndex).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(obIndex).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(obIndex).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(obIndex).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(obIndex).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(obIndex).getElement(12) & 0xFF);

        Assert.assertEquals("four messages sent", 4, lnis.outbound.size());
        Assert.assertEquals("one previous programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 3, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("no new message sent",4, lnis.outbound.size());
        Assert.assertEquals("two programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        csosa.writeCsOpSw("csOpSw.65", 1, pl);

        // should have written
        Assert.assertEquals("another messages sent", 5, obIndex = lnis.outbound.size());
        Assert.assertEquals("two previous programming replies", 2, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 4, lnis.getReceivedMsgCount());

        obIndex--;
        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(obIndex).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(obIndex).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7e, lnis.outbound.get(obIndex).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x65, lnis.outbound.get(obIndex).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(obIndex).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(obIndex).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(obIndex).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(obIndex).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(obIndex).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(obIndex).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(obIndex).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(obIndex).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(obIndex).getElement(12) & 0xFF);

        Assert.assertEquals("five messages sent", 5, lnis.outbound.size());
        Assert.assertEquals("two previous programming replies", 2, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 4, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("five message sent", 5, lnis.outbound.size());
        Assert.assertEquals("three programming replies", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        csosa.writeCsOpSw("csOpSw.1", 1, pl);

        // should have written
        Assert.assertEquals("six messages sent", 6, obIndex = lnis.outbound.size());
        Assert.assertEquals("three previous programming replies", 3, pl.getRcvdInvoked());

        obIndex--;
        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(obIndex).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(obIndex).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(obIndex).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x11, lnis.outbound.get(obIndex).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(obIndex).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(obIndex).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(obIndex).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(obIndex).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(obIndex).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(obIndex).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(obIndex).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(obIndex).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(obIndex).getElement(12) & 0xFF);

        Assert.assertEquals("six messages sent", 6, lnis.outbound.size());
        Assert.assertEquals("three previous programming replies", 3, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 5, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("six messages sent", 6, lnis.outbound.size());
        Assert.assertEquals("four programming replies", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        csosa.writeCsOpSw("csOpSw.68", 1, pl);

        // should have written
        Assert.assertEquals("seven messages sent", 7, obIndex = lnis.outbound.size());
        Assert.assertEquals("four previous programming replies", 4, pl.getRcvdInvoked());

        obIndex--;
        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(obIndex).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(obIndex).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7e, lnis.outbound.get(obIndex).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x6D, lnis.outbound.get(obIndex).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(obIndex).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(obIndex).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(obIndex).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(obIndex).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(obIndex).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(obIndex).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(obIndex).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(obIndex).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(obIndex).getElement(12) & 0xFF);

        Assert.assertEquals("received a messages", 6, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("seven messages sent", 7, lnis.outbound.size());
        Assert.assertEquals("five programming replies", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        csosa.writeCsOpSw("csOpSw.56", 1, pl);

        // should NOT have written OpSw56 (56 is evenly divisible by 8, so not writable)
        Assert.assertEquals("no additional message sent", 7, lnis.outbound.size());
        Assert.assertEquals("six programming replies", 6, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status bad", 1, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot program OpSw56 account LocoNet encoding limitations.");

        // try another write
        csosa.writeCsOpSw("csOpSw.129", 1, pl);

        // should NOT have written OpSw129 (129 is out-of-range)
        Assert.assertEquals("no additional message sent", 7, lnis.outbound.size());
        Assert.assertEquals("six programming replies", 7, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status bad", 8, pl.getRcvdStatus());
        jmri.util.JUnitAppender.assertWarnMessage("Cannot perform Cs OpSw access: parts.length=2, parts[]=[csOpSw, 129], val=1");
     }
     
// Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SlotManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

}
