package jmri.jmrix.loconet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class LocoNetThrottleTest extends TestCase {

    public LocoNetThrottleTest(String s) {
        super(s);
    }

    public void testCTor() {
        LocoNetThrottle t = new LocoNetThrottle(new LocoNetSystemConnectionMemo(lnis, slotmanager), new LocoNetSlot(0));
        Assert.assertNotNull(t);
    }

    // test the speed setting code.
    public void testSpeedSetting() {
        // we have 4 cases to check
        // Case 1: The locomotive is not consisted.
        LocoNetSlot s1 = new LocoNetSlot(0) {
            @Override
            public int consistStatus() {
                return LnConstants.CONSIST_NO;
            }

            @Override
            public int speed() {
                return 0;
            }
        };
        LocoNetThrottle t1 = new LocoNetThrottle(new LocoNetSystemConnectionMemo(lnis, slotmanager), s1);
        Assert.assertEquals(0.0f, t1.getSpeedSetting());
        t1.setSpeedSetting(0.5f);
        // the speed change SHOULD be changed.
        Assert.assertEquals(0.5f, t1.getSpeedSetting());

        // Case 2: The locomotive is a consist top.
        LocoNetSlot s2 = new LocoNetSlot(1) {
            @Override
            public int consistStatus() {
                return LnConstants.CONSIST_TOP;
            }

            @Override
            public int speed() {
                return 0;
            }
        };
        LocoNetThrottle t2 = new LocoNetThrottle(new LocoNetSystemConnectionMemo(lnis, slotmanager), s2);
        Assert.assertEquals(0.0f, t2.getSpeedSetting());
        t2.setSpeedSetting(0.5f);
        // the speed change SHOULD be changed.
        Assert.assertEquals(0.5f, t2.getSpeedSetting());

        // Case 3: The locomotive is a consist mid.
        LocoNetSlot s3 = new LocoNetSlot(2) {
            @Override
            public int consistStatus() {
                return LnConstants.CONSIST_MID;
            }

            @Override
            public int speed() {
                return 0;
            }
        };
        LocoNetThrottle t3 = new LocoNetThrottle(new LocoNetSystemConnectionMemo(lnis, slotmanager), s3);
        Assert.assertEquals(0.0f, t3.getSpeedSetting());
        t3.setSpeedSetting(0.5f);
        // the speed change SHOULD NOT be changed.
        Assert.assertEquals(0.0f, t3.getSpeedSetting());

        // Case 3: The locomotive is a consist mid.
        // make sure the speed does NOT change for a consist sub
        LocoNetSlot s4 = new LocoNetSlot(3) {
            @Override
            public int consistStatus() {
                return LnConstants.CONSIST_SUB;
            }

            @Override
            public int speed() {
                return 0;
            }
        };
        LocoNetThrottle t4 = new LocoNetThrottle(new LocoNetSystemConnectionMemo(lnis, slotmanager), s4);
        Assert.assertEquals(0.0f, t4.getSpeedSetting());
        t4.setSpeedSetting(0.5f);
        // the speed change SHOULD be ignored.
        Assert.assertEquals(0.0f, t4.getSpeedSetting());
    }

    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LocoNetThrottleTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LocoNetThrottleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);

        // set slot 3 to address 3
        LocoNetMessage m = new LocoNetMessage(13);
        m.setOpCode(LnConstants.OPC_WR_SL_DATA);
        m.setElement(1, 0x0E);
        m.setElement(2, 0x03);
        m.setElement(4, 0x03);
        slotmanager.slot(3).setSlot(m);

        // set slot 4 to address 255
        m.setElement(2, 0x04);
        m.setElement(4, 0x7F);
        m.setElement(9, 0x01);
        slotmanager.slot(4).setSlot(m);

        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
