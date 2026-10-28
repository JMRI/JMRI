package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Check that block routing work started from another thread, such as the
 * Dispatcher allocating a section, is done on the layout thread, and that the
 * routing is only reported as stable from the layout thread.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class LayoutBlockRoutingThreadTest {

    private final List<String> offLayoutThread = Collections.synchronizedList(new ArrayList<>());

    // record routing events sent from any thread but the layout thread; panel
    // redraw requests are not routing work
    private PropertyChangeListener recordThread(String what) {
        return e -> {
            if (!ThreadingUtil.isLayoutThread() && !LayoutBlock.PROPERTY_REDRAW.equals(e.getPropertyName())) {
                offLayoutThread.add(what + " " + e.getPropertyName() + " on " + Thread.currentThread().getName());
            }
        };
    }

    private static void runOnNewThread(Runnable r) throws InterruptedException {
        Thread t = new Thread(r, "LayoutBlockRoutingThreadTest");
        t.start();
        t.join();
    }

    @Test
    public void testAllocationFromAnotherThread() throws InterruptedException {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        lbm.enableAdvancedRouting(true);
        LayoutBlock layoutBlock = new LayoutBlock("ILB996", "Routing Thread Block");
        layoutBlock.initializeLayoutBlock();
        // the thread is recorded first: the test goes on once the update is counted
        layoutBlock.addPropertyChangeListener(recordThread("layout block"));
        List<Object> updates = Collections.synchronizedList(new ArrayList<>());
        layoutBlock.addPropertyChangeListener(e -> {
            if (LayoutBlock.PROPERTY_ROUTING.equals(e.getPropertyName())) {
                updates.add(e.getNewValue());
            }
        });

        runOnNewThread(() -> layoutBlock.setUseExtraColor(true));
        JUnitUtil.waitFor(() -> updates.size() == 1, "routing update sent");
        assertTrue(layoutBlock.getUseExtraColor(), "block allocated");
        assertTrue(offLayoutThread.isEmpty(), "routing update sent on the layout thread " + offLayoutThread);
        layoutBlock.dispose();
    }

    @Test
    public void testStabilisedReportedOnLayoutThread() throws InterruptedException {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        // the thread is recorded first: the test goes on once the events are counted
        lbm.addPropertyChangeListener(recordThread("manager"));
        List<Object> topology = Collections.synchronizedList(new ArrayList<>());
        lbm.addPropertyChangeListener(e -> {
            if (LayoutBlockManager.PROPERTY_TOPOLOGY.equals(e.getPropertyName())) {
                topology.add(e.getNewValue());
            }
        });

        lbm.setStabilisedDelay(20);

        // each change made once the routing has settled is reported as
        // unstable and then as stable again
        for (int round = 1; round <= 3; round++) {
            int events = 2 * round;
            runOnNewThread(lbm::setLastRoutingChange);
            JUnitUtil.waitFor(() -> topology.size() == events, "change " + round + " reported and settled");
        }
        assertEquals(List.of(false, true, false, true, false, true), topology, "topology events");
        assertTrue(lbm.routingStablised(), "stable after the changes");

        // changes from two threads while the routing keeps settling: however
        // they fall, the unstable and stable events must still alternate
        Runnable changes = () -> {
            for (int i = 0; i < 150; i++) {
                lbm.setLastRoutingChange();
                try {
                    Thread.sleep(i % 30);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        };
        Thread first = new Thread(changes, "LayoutBlockRoutingThreadTest changes 1");
        Thread second = new Thread(changes, "LayoutBlockRoutingThreadTest changes 2");
        first.start();
        second.start();
        first.join();
        second.join();
        JUnitUtil.waitFor(() -> lbm.routingStablised(), "routing stabilised again");
        // the events are posted to the layout thread in order, so once those
        // posted before the routing settled have run, the last one to arrive
        // is the final stable event
        ThreadingUtil.runOnLayout(() -> {});
        JUnitUtil.waitFor(() -> topology.get(topology.size() - 1).equals(true), "stable event sent");
        for (int i = 0; i < topology.size(); i++) {
            assertEquals(i % 2 != 0, topology.get(i), "unstable and stable events alternate, event " + i);
        }
        assertTrue(lbm.routingStablised(), "stable at the end");
        assertTrue(offLayoutThread.isEmpty(), "events sent on the layout thread " + offLayoutThread);
    }

    @Test
    @DisabledIfHeadless
    public void testAllocationsWhileMetricsChange() throws Exception {
        LayoutBlockManager lbm = LayoutBlockRoutingChecker.loadPanel("LayoutEditorTest-4-19-6.xml");
        List<LayoutBlock> blocks = new ArrayList<>(lbm.getNamedBeanSet());
        for (LayoutBlock lb : blocks) {
            lb.addPropertyChangeListener(recordThread(lb.getDisplayName()));
        }

        // allocate and free blocks from another thread while the layout thread
        // changes metrics, as the Dispatcher and a user editing blocks might
        Thread allocator = new Thread(() -> {
            for (int i = 0; i < 200; i++) {
                LayoutBlock lb = blocks.get(i % blocks.size());
                lb.setUseExtraColor((i / blocks.size()) % 2 == 0);
            }
        }, "LayoutBlockRoutingThreadTest allocator");
        allocator.start();
        for (int i = 0; i < 20; i++) {
            LayoutBlock lb = blocks.get(i % blocks.size());
            int metric = lb.getBlockMetric() + ((i % 2 == 0) ? 7 : -7);
            ThreadingUtil.runOnLayout(() -> lb.setBlockMetric(metric));
        }
        allocator.join();
        ThreadingUtil.runOnLayout(() -> {}); // let the posted updates run
        assertTrue(offLayoutThread.isEmpty(), "routing done on the layout thread, but "
                + offLayoutThread.size() + " events were not, first: "
                + offLayoutThread.subList(0, Math.min(3, offLayoutThread.size())));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initMemoryManager();
    }

    @AfterEach
    public void tearDown() {
        for (LayoutEditor panel : InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class)) {
            JUnitUtil.dispose(panel);
        }
        JUnitUtil.clearRouteThreads();
        JUnitUtil.clearTurnoutThreads();
        JUnitUtil.removeMatchingThreads("Routing stabilising timer");
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
