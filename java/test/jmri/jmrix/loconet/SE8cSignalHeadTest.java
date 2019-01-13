package jmri.jmrix.loconet;

import java.beans.PropertyChangeListener;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        sh1.setAppearance(jmri.SignalHead.FLASHGREEN);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.FLASHGREEN, sh1.getAppearance());
        Assert.assertEquals("now four transmitted message", 4, lnis.outbound.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, lnis.outbound.get(3).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x0F, lnis.outbound.get(3).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, lnis.outbound.get(3).getElement(2));

        jmri.util.JUnitUtil.fasterWaitFor(()->{return lnis.outbound.size() >= 5;},"message received");

        Assert.assertEquals("now five transmitted message", 5, lnis.outbound.size());
        Assert.assertEquals("transmit message byte 1", 0x10, lnis.outbound.get(4).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, lnis.outbound.get(4).getElement(2));

        sh1.setAppearance(jmri.SignalHead.FLASHRED);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.FLASHRED, sh1.getAppearance());
        Assert.assertEquals("now six transmitted message", 6, lnis.outbound.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, lnis.outbound.get(5).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x10, lnis.outbound.get(5).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, lnis.outbound.get(5).getElement(2));

        jmri.util.JUnitUtil.fasterWaitFor(()->{return lnis.outbound.size() >= 7;},"message received");

        Assert.assertEquals("now seven transmitted message", 7, lnis.outbound.size());
        Assert.assertEquals("transmit message byte 1", 0x0f, lnis.outbound.get(6).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x10, lnis.outbound.get(6).getElement(2));

        sh1.setAppearance(jmri.SignalHead.FLASHYELLOW);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.FLASHYELLOW, sh1.getAppearance());
        Assert.assertEquals("now eight transmitted message", 8, lnis.outbound.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, lnis.outbound.get(7).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x10, lnis.outbound.get(7).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x10, lnis.outbound.get(7).getElement(2));

        jmri.util.JUnitUtil.fasterWaitFor(()->{return lnis.outbound.size() >= 9;},"message received");

        Assert.assertEquals("now nine transmitted message", 9, lnis.outbound.size());
        Assert.assertEquals("transmit message byte 1", 0x10, lnis.outbound.get(8).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, lnis.outbound.get(8).getElement(2));

        sh1.setAppearance(jmri.SignalHead.DARK);
        Assert.assertEquals("appearance was changed", jmri.SignalHead.DARK, sh1.getAppearance());
        Assert.assertEquals("now ten transmitted message", 10, lnis.outbound.size());
        Assert.assertEquals("transmit message OpCode", LnConstants.OPC_SW_REQ, lnis.outbound.get(9).getOpCode());
        Assert.assertEquals("transmit message byte 1", 0x10, lnis.outbound.get(9).getElement(1));
        Assert.assertEquals("transmit message Byte 2", 0x30, lnis.outbound.get(9).getElement(2));

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

        propChangeFlag=false;
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

    jmri.jmrix.loconet.LocoNetInterfaceScaffold lnis;
    LocoNetSystemConnectionMemo memo;
    boolean propChangeFlag;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.resetProfileManager();

        memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        jmri.InstanceManager.store(lnis, jmri.jmrix.loconet.LnTrafficController.class);
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LnTrafficController.class, lnis);

        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100,false,false,false);
        // memo.configureManagers(); // Skip this step, else autonomous loconet traffic is generated!
        jmri.InstanceManager.store(memo,jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class);
        propChangeFlag=false;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SE8cSignalHeadTest.class);

}
