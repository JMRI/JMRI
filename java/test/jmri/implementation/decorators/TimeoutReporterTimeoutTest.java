package jmri.implementation.decorators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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
        AtomicReference<Boolean> clearedOnLayoutThread = new AtomicReference<>();
        r.addPropertyChangeListener(Reporter.PROPERTY_CURRENT_REPORT, e -> {
            if (e.getNewValue() == null) {
                clearedOnLayoutThread.set(ThreadingUtil.isLayoutThread());
            }
        });
        read(tag);
        assertEquals(tag, r.getCurrentReport(), "tag read");
        // The report is cleared before its listeners are told, so wait for the
        // listener, not for the report.
        JUnitUtil.waitFor(() -> clearedOnLayoutThread.get() != null, "listeners told the report was cleared");
        assertEquals(Boolean.TRUE, clearedOnLayoutThread.get(), "listeners told on the layout thread");
        assertNull(r.getCurrentReport(), "report cleared");
    }

    @Test
    public void testSameTagSeenAgainRestartsTimeout() throws InterruptedException {
        AtomicLong clearedAt = new AtomicLong();
        r.addPropertyChangeListener(Reporter.PROPERTY_CURRENT_REPORT, e -> {
            if (e.getNewValue() == null) {
                clearedAt.compareAndSet(0, System.currentTimeMillis());
            }
        });
        read(tag);
        // Well inside the first timeout: the second read has TIMEOUT * 3 / 4 to
        // happen before the first timeout would end.
        Thread.sleep(TIMEOUT / 4);
        long secondRead = System.currentTimeMillis();
        read(tag);     // the reader sees the same tag again
        JUnitUtil.waitFor(() -> clearedAt.get() != 0, "report cleared");
        // A timeout never ends early, so a clear before a full timeout after the
        // second read can only be the first timeout, which the second read
        // should have replaced.
        assertTrue(clearedAt.get() - secondRead >= TIMEOUT,
            "cleared a full timeout after the second read, not when the first timeout ended");
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
