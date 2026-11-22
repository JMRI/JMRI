package jmri.jmrix.can.adapters.gridconnect;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.jmrix.AbstractPortControllerScaffold;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests the least time between frames a GridConnect traffic controller writes
 * to its port.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class GcTrafficControllerFrameGapTest {

    @Test
    public void testNoGapByDefault() {
        Assertions.assertEquals(0, tc.getFrameGapMillis(), "no gap unless one is set");
        tc.setFrameGapMillis(-5);
        Assertions.assertEquals(0, tc.getFrameGapMillis(), "a negative gap is none");
    }

    @Timeout(20)
    @Test
    public void testFramesWrittenAtLeastTheGapApart() throws IOException {
        int gap = 40;
        tc.setFrameGapMillis(gap);
        tc.connectPort(pcs);
        for (int i = 0; i < 4; i++) {
            tc.sendCanMessage(new CanMessage(new int[]{0x90, 0x00, 0x62, 0x00, 0xC8 + i}, 0x7A), null);
        }
        JUnitUtil.waitFor(() -> written.size() == 4, "4 frames written");
        for (int i = 1; i < written.size(); i++) {
            long apart = (written.get(i) - written.get(i - 1)) / 1_000_000L;
            Assertions.assertTrue(apart >= gap - 1, "frame " + i + " written " + apart + " ms after the one before, not " + gap);
        }
    }

    @Timeout(20)
    @Test
    public void testNoWaitWithoutAGap() throws IOException {
        tc.connectPort(pcs);
        for (int i = 0; i < 4; i++) {
            tc.sendCanMessage(new CanMessage(new int[]{0x90, 0x00, 0x62, 0x00, 0xC8 + i}, 0x7A), null);
        }
        JUnitUtil.waitFor(() -> written.size() == 4, "4 frames written");
        long all = (written.get(3) - written.get(0)) / 1_000_000L;
        Assertions.assertTrue(all < 1000, "4 frames written within " + all + " ms");
    }

    // the time each frame (ending ';') reaches the port's output stream
    private final List<Long> written = Collections.synchronizedList(new ArrayList<>());
    private GcTrafficController tc;
    private AbstractPortControllerScaffold pcs;

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new GcTrafficController();
        DataOutputStream out = new DataOutputStream(new OutputStream() {
            @Override
            public void write(int b) {
                if (b == ';') {
                    written.add(System.nanoTime());
                }
            }
        });
        pcs = new AbstractPortControllerScaffold(new CanSystemConnectionMemo()) {
            @Override
            public DataOutputStream getOutputStream() {
                return out;
            }
        };
    }

    @AfterEach
    public void tearDown() {
        tc.terminateThreads();
        pcs.dispose();
        JUnitUtil.tearDown();
    }

}
