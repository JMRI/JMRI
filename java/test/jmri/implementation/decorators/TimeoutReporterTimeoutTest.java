package jmri.implementation.decorators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;

import jmri.IdTag;
import jmri.Reporter;
import jmri.implementation.AbstractIdTagReporter;
import jmri.implementation.DefaultIdTag;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the timeout of a TimeoutReporter, as used for MERG Concentrator
 * and stand-alone RFID readers.
 */
public class TimeoutReporterTimeoutTest {

    private static final int TIMEOUT = 600;

    private TimeoutReporter r;
    private IdTag tag;

    private void read(IdTag t) {
        ThreadingUtil.runOnLayout(() -> r.notify(t));
    }

    @Test
    public void testReportClearedOnLayoutThread() {
        AtomicBoolean clearedOnLayoutThread = new AtomicBoolean(false);
        r.addPropertyChangeListener(Reporter.PROPERTY_CURRENT_REPORT, e -> {
            if (e.getNewValue() == null) {
                clearedOnLayoutThread.set(ThreadingUtil.isLayoutThread());
            }
        });
        read(tag);
        assertEquals(tag, r.getCurrentReport(), "tag read");
        JUnitUtil.waitFor(() -> r.getCurrentReport() == null, "report cleared");
        assertTrue(clearedOnLayoutThread.get(), "listeners told on the layout thread");
    }

    @Test
    public void testSameTagSeenAgainRestartsTimeout() throws InterruptedException {
        long start = System.currentTimeMillis();
        read(tag);
        Thread.sleep(TIMEOUT / 2);
        read(tag);     // the reader sees the same tag again
        long wait = start + TIMEOUT + TIMEOUT / 4 - System.currentTimeMillis();
        if (wait > 0) {
            Thread.sleep(wait);
        }
        assertEquals(tag, r.getCurrentReport(), "tag still reported, its timeout started again");
        JUnitUtil.waitFor(() -> r.getCurrentReport() == null, "report cleared");
    }

    @Test
    public void testEachNewReportTimesOut() {
        for (int i = 0; i < 3; i++) {
            read(tag);
            assertEquals(tag, r.getCurrentReport(), "tag read " + i);
            JUnitUtil.waitFor(() -> r.getCurrentReport() == null, "report cleared " + i);
        }
        assertNull(r.getCurrentReport(), "no report");
        assertEquals(tag, r.getLastReport(), "last report kept");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initIdTagManager();
        r = new TimeoutReporter(new AbstractIdTagReporter("IR1"));
        r.setTimeout(TIMEOUT);
        tag = new DefaultIdTag("ID1234");
    }

    @AfterEach
    public void tearDown() {
        r.dispose();
        r = null;
        JUnitUtil.removeMatchingThreads("Timeout-");
        JUnitUtil.tearDown();
    }

}
