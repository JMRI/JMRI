package jmri.jmrix.openlcb.swing.lccpro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingUtilities;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.openlcb.*;

/**
 * Tests for LccProTableModel.
 */
public class LccProTableModelTest {

    @Test
    public void testNodeReplyUpdatesTable() {
        MimicNodeStore.NodeMemo node = store.addNode(new NodeID("2.3.4.5.6.7"));
        JUnitUtil.waitFor(() -> changes.get() > 0, "table updated for the new node");
        int before = changes.get();

        // a PIP reply arriving for the node, as from the network
        node.handleProtocolIdentificationReply(
                new ProtocolIdentificationReplyMessage(node.getNodeID(), nidSource, 0x800000000000L), connection);

        JUnitUtil.waitFor(() -> changes.get() > before, "table updated for the reply");
    }

    @Test
    public void testNodeReplyDoesNotWaitForSwing() throws InterruptedException {
        MimicNodeStore.NodeMemo node = store.addNode(new NodeID("2.3.4.5.6.7"));
        JUnitUtil.waitFor(() -> changes.get() > 0, "table updated for the new node");

        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch swingHeld = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            swingHeld.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        swingHeld.await();
        try {
            // the reply is handled on another thread while Swing is busy
            Thread network = new Thread(() -> node.handleProtocolIdentificationReply(
                    new ProtocolIdentificationReplyMessage(node.getNodeID(), nidSource, 0x800000000000L), connection),
                    "network");
            network.start();
            // it must finish while Swing is still held, not wait for it
            JUnitUtil.waitFor(() -> !network.isAlive(), "reply handled without waiting for Swing");
        } finally {
            release.countDown();
        }
    }

    @Test
    public void testBurstOfRepliesGivesOneUpdate() throws InterruptedException {
        MimicNodeStore.NodeMemo node = store.addNode(new NodeID("2.3.4.5.6.7"));
        JUnitUtil.waitFor(() -> changes.get() > 0, "table updated for the new node");
        JUnitUtil.waitFor(() -> pendingSwingWorkDone(), "Swing idle");
        int before = changes.get();

        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch swingHeld = new CountDownLatch(1);
        SwingUtilities.invokeLater(() -> {
            swingHeld.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        swingHeld.await();
        Thread network = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                node.handleProtocolIdentificationReply(
                        new ProtocolIdentificationReplyMessage(node.getNodeID(), nidSource, 0x800000000000L), connection);
            }
        }, "network");
        try {
            network.start();
            JUnitUtil.waitFor(() -> !network.isAlive(), "replies handled without waiting for Swing");
        } finally {
            release.countDown();
        }
        JUnitUtil.waitFor(() -> pendingSwingWorkDone(), "Swing idle");
        assertEquals(before + 1, changes.get(), "one update for the burst");

        // a later reply gives a later update
        node.handleProtocolIdentificationReply(
                new ProtocolIdentificationReplyMessage(node.getNodeID(), nidSource, 0x800000000000L), connection);
        JUnitUtil.waitFor(() -> changes.get() == before + 2, "update for the later reply");
    }

    /**
     * Run an empty task on the Swing thread and wait for it, so that
     * everything queued before it has run.
     */
    private static boolean pendingSwingWorkDone() {
        try {
            SwingUtilities.invokeAndWait(() -> {});
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ***********   Infrastructure Below *************

    final NodeID nidSource = new NodeID(new byte[]{0, 0, 0, 0, 0, 1});

    final Connection connection = new AbstractConnection() {
        @Override
        public void put(Message msg, Connection sender) {
        }
    };

    MimicNodeStore store;
    CanSystemConnectionMemo memo;
    LccProTableModel model;
    final AtomicInteger changes = new AtomicInteger();

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        store = new MimicNodeStore(connection, nidSource);
        memo = new CanSystemConnectionMemo();
        memo.store(store, MimicNodeStore.class);
        model = new LccProTableModel(memo);
        assertNotNull(model);
        model.addTableModelListener(e -> changes.incrementAndGet());
    }

    @AfterEach
    public void tearDown() {
        model.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

}
