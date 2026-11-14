package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.IdTag;
import jmri.ReporterManager;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.ConfigurationManager;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the erase-on-timeout option of the CBUS Reporter.
 */
public class CbusReporterTimeoutTest {

    private static final int TIMEOUT = 600;

    private TrafficControllerScaffold tcis;
    private CanSystemConnectionMemo memo;
    private CbusReporter r;

    private void readTag() {
        CanReply m = new CanReply(tcis.getCanid());
        m.setNumDataElements(8);
        m.setElement(0, CbusConstants.CBUS_ACDAT);
        m.setElement(1, 0x00); // device hi
        m.setElement(2, 0x01); // device lo
        m.setElement(3, 0x30); // tag1
        m.setElement(4, 0x39); // tag2
        m.setElement(5, 0x31); // tag3
        m.setElement(6, 0x30); // tag4
        m.setElement(7, 0xAB); // tag5
        jmri.util.ThreadingUtil.runOnLayout(() -> r.reply(m));
    }

    @Test
    public void testSecondReadOfSameTagRestartsTimeout() throws InterruptedException {
        long start = System.currentTimeMillis();
        readTag();
        IdTag tag = (IdTag) r.getCurrentReport();
        assertNotNull(tag, "tag read");

        // the same tag is read again half way through the timeout
        Thread.sleep(TIMEOUT / 2);
        readTag();
        assertEquals(tag, r.getCurrentReport(), "same tag");

        // after the first read's timeout, but before the second one's
        long wait = start + TIMEOUT + TIMEOUT / 4 - System.currentTimeMillis();
        if (wait > 0) {
            Thread.sleep(wait);
        }
        assertEquals(tag, r.getCurrentReport(), "tag still reported, its timeout started again");

        // the report is cleared after the second read's timeout
        JUnitUtil.waitFor(() -> r.getCurrentReport() == null, "report cleared");
    }

    @Test
    public void testReportClearedAfterTimeout() {
        readTag();
        assertNotNull(r.getCurrentReport(), "tag read");
        JUnitUtil.waitFor(() -> r.getCurrentReport() == null, "report cleared");
        assertEquals(IdTag.UNSEEN, r.getState(), "reporter state");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        memo.setProtocol(ConfigurationManager.MERGCBUS);
        memo.configureManagers();
        CbusReporterManager rm = (CbusReporterManager) memo.get(ReporterManager.class);
        rm.setTimeout(TIMEOUT);
        r = (CbusReporter) rm.provide("1");
        r.eraseOnTimeoutThisReporter = true;
    }

    @AfterEach
    public void tearDown() {
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).dispose();
        r.dispose();
        memo.dispose();
        memo = null;
        tcis.terminateThreads();
        tcis = null;
        r = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
