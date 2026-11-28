package jmri.jmrix.loconet;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;

import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SE8cSignalHeadTest {

    @Test
    public void testCTor() {
        SE8cSignalHead t = new SE8cSignalHead(5);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testIt() {
        SE8cSignalHead sh1 = new SE8cSignalHead(12);
        Assert.assertEquals("default appearance upon construction", jmri.SignalHead.DARK, sh1.getAppearance());
        Assert.assertEquals("signal head number assigned", 12, sh1.getNumber());
        Assert.assertNull("User Name check", sh1.getUserName());

        sh1.init(14);
        Assert.assertEquals("signal head number changed", 14, sh1.getNumber());

        sh1.setAppearance(jmri.SignalHead.GREEN);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.GREEN, sh1.getAppearance());

        sh1.setAppearance(jmri.SignalHead.RED);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.RED, sh1.getAppearance());

        sh1.setAppearance(jmri.SignalHead.YELLOW);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.YELLOW, sh1.getAppearance());

        sh1 = new SE8cSignalHead(16, "Test Head 2");
        Assert.assertEquals("signal head number assigned", 16, sh1.getNumber());
        Assert.assertEquals("User Name check", "Test Head 2", sh1.getUserName());

        lnis.outbound.removeAllElements();
        Assert.assertEquals("no transmitted messages yet", 0, lnis.outbound.size());
        sh1.updateOutput();
        Assert.assertEquals("appearance was changed", jmri.SignalHead.DARK, sh1.getAppearance());
        Assert.assertEquals("one transmitted message", 1, lnis.outbound.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x10, lnis.outbound.get(0).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, lnis.outbound.get(0).getElement(2));

        sh1.setAppearance(jmri.SignalHead.YELLOW);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.YELLOW, sh1.getAppearance());
        Assert.assertEquals("now two transmitted message", 2, lnis.outbound.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x10, lnis.outbound.get(1).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x10, lnis.outbound.get(1).getElement(2));

        sh1.setAppearance(jmri.SignalHead.GREEN);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.GREEN, sh1.getAppearance());
        Assert.assertEquals("now three transmitted message", 3, lnis.outbound.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, lnis.outbound.get(2).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x0F, lnis.outbound.get(2).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, lnis.outbound.get(2).getElement(2));

        sh1.setAppearance(jmri.SignalHead.RED);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.RED, sh1.getAppearance());
        Assert.assertEquals("now three transmitted message", 4, lnis.outbound.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, lnis.outbound.get(3).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x0F, lnis.outbound.get(3).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x10, lnis.outbound.get(3).getElement(2));

        sh1.message(lnis.outbound.get(2));
        Assert.assertEquals("appearance was changed", jmri.SignalHead.GREEN, sh1.getAppearance());

        sh1.message(lnis.outbound.get(1));
        Assert.assertEquals("appearance was changed", jmri.SignalHead.YELLOW, sh1.getAppearance());

        sh1.message(lnis.outbound.get(3));
        Assert.assertEquals("appearance was changed", jmri.SignalHead.RED, sh1.getAppearance());

        sh1.message(lnis.outbound.get(0));
        Assert.assertEquals("appearance was changed", jmri.SignalHead.DARK, sh1.getAppearance());

        sh1.message(lnis.outbound.get(3));
        Assert.assertEquals("appearance was changed", jmri.SignalHead.RED, sh1.getAppearance());

        Assert.assertEquals("a bunch of transmitted messages", 4, lnis.outbound.size());
        lnis.outbound.removeAllElements();
        Assert.assertEquals("no transmitted messages yet", 0, lnis.outbound.size());
        Assert.assertEquals("appearance is", jmri.SignalHead.RED, sh1.getAppearance());
        sh1.updateOutput();
        Assert.assertEquals("one transmitted messages", 1, lnis.outbound.size());

        sh1.setLit(false);
        Assert.assertEquals("two transmitted messages", 2, lnis.outbound.size());
        Assert.assertEquals("appearance was changed", jmri.SignalHead.RED, sh1.getAppearance());
        Assert.assertEquals("checking message opcode from setting unlit", 0xb0, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("transmit message byte 1 from setting unlit", 0x10, lnis.outbound.get(1).getElement(1));
        Assert.assertEquals("transmit message Byte 2 from setting unlit", 0x30, lnis.outbound.get(1).getElement(2));

        sh1.setLit(true);
        Assert.assertEquals("now three transmitted message", 3, lnis.outbound.size());

        // While the head flashes, its timer sends a message on the GUI thread (Swing thread)
        // every DefaultSignalHead.masterDelay (750 ms), and what a change of appearance sends
        // depends on whether the head is then in the lit or the dark half of a flash. So each
        // change below is made on the GUI thread just after the timer's next flash message,
        // before the timer can send another, however long this thread is held up.
        SE8cSignalHead head = sh1;
        AfterFlash toDark = new AfterFlash(head, jmri.SignalHead.DARK, null);
        AfterFlash toFlashYellow = new AfterFlash(head, jmri.SignalHead.FLASHYELLOW, toDark);
        AfterFlash toFlashRed = new AfterFlash(head, jmri.SignalHead.FLASHRED, toFlashYellow);
        AfterFlash toFlashGreen = new AfterFlash(head, jmri.SignalHead.FLASHGREEN, toFlashRed);
        ThreadingUtil.runOnGUI(toFlashGreen::run); // not after a flash: the head isn't flashing yet
        Assert.assertEquals("appearance was changed", jmri.SignalHead.FLASHGREEN, toFlashGreen.appearanceAfter);
        Assert.assertEquals("now four transmitted message", 4, toFlashGreen.after.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, toFlashGreen.after.get(3).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x0F, toFlashGreen.after.get(3).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, toFlashGreen.after.get(3).getElement(2));

        toFlashRed.waitDone();
        Assert.assertEquals("now five transmitted message", 5, toFlashRed.before.size());
        Assert.assertEquals("transmit message byte 1", 0x10, toFlashRed.before.get(4).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, toFlashRed.before.get(4).getElement(2));

        Assert.assertEquals("appearance was changed", jmri.SignalHead.FLASHRED, toFlashRed.appearanceAfter);
        Assert.assertEquals("now six transmitted message", 6, toFlashRed.after.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, toFlashRed.after.get(5).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x10, toFlashRed.after.get(5).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, toFlashRed.after.get(5).getElement(2));

        toFlashYellow.waitDone();
        Assert.assertEquals("now seven transmitted message", 7, toFlashYellow.before.size());
        Assert.assertEquals("transmit message byte 1", 0x0f, toFlashYellow.before.get(6).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x10, toFlashYellow.before.get(6).getElement(2));

        Assert.assertEquals("appearance was changed", jmri.SignalHead.FLASHYELLOW, toFlashYellow.appearanceAfter);
        Assert.assertEquals("now eight transmitted message", 8, toFlashYellow.after.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, toFlashYellow.after.get(7).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x10, toFlashYellow.after.get(7).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x10, toFlashYellow.after.get(7).getElement(2));

        toDark.waitDone();
        Assert.assertEquals("now nine transmitted message", 9, toDark.before.size());
        Assert.assertEquals("transmit message byte 1", 0x10, toDark.before.get(8).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, toDark.before.get(8).getElement(2));

        Assert.assertEquals("appearance was changed", jmri.SignalHead.DARK, toDark.appearanceAfter);
        Assert.assertEquals("now ten transmitted message", 10, toDark.after.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, toDark.after.get(9).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x10, toDark.after.get(9).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, toDark.after.get(9).getElement(2));

        sh1.message(lnis.outbound.get(0));
        Assert.assertEquals("receive message", jmri.SignalHead.RED, sh1.getAppearance());

        sh1.message(lnis.outbound.get(3));
        Assert.assertEquals("receive message", jmri.SignalHead.GREEN, sh1.getAppearance());

        sh1.message(lnis.outbound.get(6));
        Assert.assertEquals("receive message", jmri.SignalHead.RED, sh1.getAppearance());

        sh1.message(lnis.outbound.get(7));
        Assert.assertEquals("receive message", jmri.SignalHead.YELLOW, sh1.getAppearance());

        sh1.message(lnis.outbound.get(8));
        Assert.assertEquals("receive message", jmri.SignalHead.DARK, sh1.getAppearance());

        LocoNetMessage ln2 = lnis.outbound.get(0);
        ln2.setOpCode(0xb1);
        sh1.message(ln2);
        Assert.assertEquals("receive message", jmri.SignalHead.RED, sh1.getAppearance());

        ln2 = lnis.outbound.get(3);
        ln2.setOpCode(0xb1);
        ln2.setElement(2, 0x0f);
        ln2.setElement(2, 0x20);
        sh1.message(ln2);
        Assert.assertEquals("receive message", jmri.SignalHead.GREEN, sh1.getAppearance());

        ln2 = lnis.outbound.get(6);
        ln2.setOpCode(0xb1);
        sh1.message(ln2);
        Assert.assertEquals("receive message", jmri.SignalHead.RED, sh1.getAppearance());

        ln2 = lnis.outbound.get(7);
        ln2.setOpCode(0xb1);
        sh1.message(ln2);
        Assert.assertEquals("receive message", jmri.SignalHead.YELLOW, sh1.getAppearance());

        ln2 = lnis.outbound.get(8);
        ln2.setOpCode(0xb1);
        ln2.setElement(2, 0x20);
        sh1.message(ln2);
        Assert.assertEquals("receive message", jmri.SignalHead.DARK, sh1.getAppearance());

        ln2 = lnis.outbound.get(7);
        ln2.setOpCode(0xb3);
        sh1.message(ln2);
        Assert.assertEquals("receive message", jmri.SignalHead.DARK, sh1.getAppearance());

        Assert.assertEquals("number of listeners", 0, sh1.getListenerRefs().size());

        Assert.assertFalse(propChangeFlag);
        PropertyChangeListener pcl = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                propChangeFlag = true;
            }
        };

        PropertyChangeListener[] listeners= memo.getPropertyChangeListeners();
        for (int i = 0; i < listeners.length; ++i) {
            if (listeners[i].equals(pcl)) {
                Assert.fail("did not expect to find the property change listener registered yet!");
            }
        }

        boolean foundIt = false;
        sh1.addPropertyChangeListener(pcl);
        for (int i = 0; i < listeners.length; ++i) {
            if (listeners[i].equals(pcl)) {
                foundIt = true;
            }
        }
        Assert.assertFalse("did not expect to find the property change listener registered yet!", foundIt);
        sh1.dispose();

        listeners= memo.getPropertyChangeListeners();

        for (int i = 0; i < listeners.length; ++i) {
            if (listeners[i].equals(pcl)) {
                Assert.fail("Property change listener still registered after dispose()");
            }
        }

    }

    /**
     * A change of appearance made on the GUI thread (Swing thread), for a flashing
     * head just after its flash timer's next message, before the timer can send
     * another. Records the messages sent before and after the change, and then
     * sets up the next change, if any, to follow the next flash message.
     */
    private class AfterFlash implements Runnable {

        private final SE8cSignalHead head;
        private final int appearance;
        private final AfterFlash next;
        private volatile boolean done = false;
        // written on the GUI thread before done is set, read on the test thread after
        List<LocoNetMessage> before;
        List<LocoNetMessage> after;
        int appearanceAfter;

        AfterFlash(SE8cSignalHead head, int appearance, AfterFlash next) {
            this.head = head;
            this.appearance = appearance;
            this.next = next;
        }

        @Override
        public void run() {
            before = new ArrayList<>(lnis.outbound);
            head.setAppearance(appearance);
            appearanceAfter = head.getAppearance();
            after = new ArrayList<>(lnis.outbound);
            if (next != null) {
                afterNextFlash.set(next);
            }
            done = true;
        }

        void waitDone() {
            JUnitUtil.waitFor(() -> done, "flash message sent and the change after it made");
        }
    }

    /** The change to make on the GUI thread after the next message sent, if any. */
    private final AtomicReference<AfterFlash> afterNextFlash = new AtomicReference<>();

    jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis;
    LocoNetSystemConnectionMemo memo;
    boolean propChangeFlag;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo) {
            @Override
            public void sendLocoNetMessage(LocoNetMessage m, boolean requestIgnoreEcho) {
                super.sendLocoNetMessage(m, requestIgnoreEcho);
                // the flash timer sends on the GUI thread; a change queued now runs before its next message
                AfterFlash change = afterNextFlash.getAndSet(null);
                if (change != null) {
                    SwingUtilities.invokeLater(change);
                }
            }
        };
        memo.setLnTrafficController(lnis);
        jmri.InstanceManager.store(lnis, jmri.jmrix.loconet.LnTrafficController.class);
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LnTrafficController.class, lnis);

        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false,false,false);
        // memo.configureManagers(); // Skip this step, else autonomous loconet traffic is generated!
        jmri.InstanceManager.store(memo,jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);
        propChangeFlag=false;
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(SE8cSignalHeadTest.class);

}
