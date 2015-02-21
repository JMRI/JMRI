package jmri.jmrix.loconet;

import jmri.ProgListener;
import jmri.managers.DefaultProgrammerManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SlotManagerTest extends TestCase {

    public SlotManagerTest(String s) {
        super(s);
    }

    /**
     * Local member to recall when a SlotListener has been invoked.
     */
    private LocoNetSlot testSlot;

    public void testGetDirectFunctionAddressOK() {
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetMessage m1;

        m1 = new LocoNetMessage(11);
        m1.setElement(0, 0xED);  // short 03 sets F9
        m1.setElement(1, 0x0B);
        m1.setElement(2, 0x7F);
        m1.setElement(3, 0x24);
        m1.setElement(4, 0x02);
        m1.setElement(5, 0x03);
        m1.setElement(6, 0x21);
        m1.setElement(7, 0x00);
        m1.setElement(8, 0x00);
        m1.setElement(9, 0x00);
        m1.setElement(10, 0x62);
        Assert.assertEquals("short 3 sets F9", 3,
                slotmanager.getDirectFunctionAddress(m1));

        m1 = new LocoNetMessage(11);
        m1.setElement(0, 0xED);  // long 513 sets F9
        m1.setElement(1, 0x0B);
        m1.setElement(2, 0x7F);
        m1.setElement(3, 0x34);
        m1.setElement(4, 0x05);
        m1.setElement(5, 0x42);
        m1.setElement(6, 0x01);
        m1.setElement(7, 0x21);
        m1.setElement(8, 0x00);
        m1.setElement(9, 0x00);
        m1.setElement(10, 0x35);
        Assert.assertEquals("long 513 sets F9", 513,
                slotmanager.getDirectFunctionAddress(m1));
    }

    public void testGetDirectDccPacketOK() {
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetMessage m1;

        m1 = new LocoNetMessage(11);
        m1.setElement(0, 0xED);  // short 03 sets F9
        m1.setElement(1, 0x0B);
        m1.setElement(2, 0x7F);
        m1.setElement(3, 0x24);
        m1.setElement(4, 0x02);
        m1.setElement(5, 0x03);
        m1.setElement(6, 0x21);
        m1.setElement(7, 0x00);
        m1.setElement(8, 0x00);
        m1.setElement(9, 0x00);
        m1.setElement(10, 0x62);
        Assert.assertEquals("short 3 sets F9", 0xA1,
                slotmanager.getDirectDccPacket(m1));

        m1 = new LocoNetMessage(11);
        m1.setElement(0, 0xED);  // long 513 sets F9
        m1.setElement(1, 0x0B);
        m1.setElement(2, 0x7F);
        m1.setElement(3, 0x34);
        m1.setElement(4, 0x05);
        m1.setElement(5, 0x42);
        m1.setElement(6, 0x01);
        m1.setElement(7, 0x21);
        m1.setElement(8, 0x00);
        m1.setElement(9, 0x00);
        m1.setElement(10, 0x35);
        Assert.assertEquals("long 513 sets F9", 0xA1,
                slotmanager.getDirectDccPacket(m1));
    }

    public void testGetSlotSend() {
        SlotManager slotmanager = new SlotManager(lnis);
        testSlot = null;
        SlotListener p2 = new SlotListener() {
            public void notifyChangedSlot(LocoNetSlot l) {
                testSlot = l;
            }
        };
        slotmanager.slotFromLocoAddress(0x2134, p2);
        Assert.assertEquals("slot request message",
                "BF 42 34 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
        Assert.assertEquals("hash length", 1, slotmanager.mLocoAddrHash.size());
        Assert.assertEquals("key present", true,
                slotmanager.mLocoAddrHash.containsKey(Integer.valueOf(0x2134)));
        Assert.assertEquals("value present", true,
                slotmanager.mLocoAddrHash.contains(p2));
    }

    public void testGetSlotRcv() {
        SlotManager slotmanager = new SlotManager(lnis);
        testSlot = null;
        SlotListener p2 = new SlotListener() {
            public void notifyChangedSlot(LocoNetSlot l) {
                testSlot = l;
            }
        };
        slotmanager.slotFromLocoAddress(0x2134, p2);
        // echo of the original message
        LocoNetMessage m1 = new LocoNetMessage(4);
        m1.setOpCode(0xBF);
        m1.setElement(1, 0x42);
        m1.setElement(2, 0x34);
        slotmanager.message(m1);
        // reply says its in slot 4
        LocoNetMessage m2 = new LocoNetMessage(14);
        m2.setElement(0, 0xE7);
        m2.setElement(1, 0xE);
        m2.setElement(2, 0xB);
        m2.setElement(3, 3);
        m2.setElement(4, 0x34);
        m2.setElement(5, 0);
        m2.setElement(6, 0);
        m2.setElement(7, 4);
        m2.setElement(8, 0);
        m2.setElement(9, 0x42);
        m2.setElement(10, 0);
        m2.setElement(11, 0);
        m2.setElement(12, 0);
        m2.setElement(13, 0x6c);
        slotmanager.message(m2);
        Assert.assertEquals("returned slot", slotmanager.slot(11), testSlot);
        // and make sure it forgets
        testSlot = null;
        slotmanager.message(m1);
        slotmanager.message(m2);
        Assert.assertEquals("returned slot", null, testSlot);
    }

    public void testReadCVPaged() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 12;
        ProgListener p2 = null;
        slotmanager.setMode(DefaultProgrammerManager.PAGEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 23 00 00 00 00 02 0B 7F 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    // Test names ending with "String" are for the new writeCV(String, ...) 
    // etc methods.  If you remove the older writeCV(int, ...) tests, 
    // you can rename these. Note that not all (int,...) tests may have a 
    // String(String, ...) test defined, in which case you should create those.
    public void testReadCVPagedString() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        String CV1 = "12";
        ProgListener p2 = null;
        slotmanager.setMode(DefaultProgrammerManager.PAGEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 23 00 00 00 00 02 0B 7F 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testReadCVRegister() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 2;
        ProgListener p2 = null;
        slotmanager.setMode(DefaultProgrammerManager.REGISTERMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 13 00 00 00 00 02 01 7F 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testReadCVRegisterString() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        String CV1 = "2";
        ProgListener p2 = null;
        slotmanager.setMode(DefaultProgrammerManager.REGISTERMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 13 00 00 00 00 02 01 7F 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testReadCVDirect() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 12;
        ProgListener p2 = null;
        slotmanager.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "EF 0E 7C 2B 00 00 00 00 02 0B 7F 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testReadCVOpsModeLong() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 12;
        ProgListener p2 = null;
        slotmanager.readCVOpsMode(CV1, p2, 4 * 128 + 0x23, true);
        Assert.assertEquals("read message",
                "EF 0E 7C 2F 00 04 23 00 02 0B 7F 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testReadCVOpsModeShort() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 12;
        ProgListener p2 = null;
        slotmanager.readCVOpsMode(CV1, p2, 22, false);
        Assert.assertEquals("read message",
                "EF 0E 7C 2F 00 00 16 00 02 0B 7F 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testWriteCVPaged() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 12;
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(DefaultProgrammerManager.PAGEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 63 00 00 00 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testWriteCVPagedString() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        String CV1 = "12";
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(DefaultProgrammerManager.PAGEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 63 00 00 00 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testWriteCVRegister() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 2;
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(DefaultProgrammerManager.REGISTERMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 53 00 00 00 00 00 01 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testWriteCVDirect() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 12;
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 6B 00 00 00 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testWriteCVDirectString() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        String CV1 = "12";
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "EF 0E 7C 6B 00 00 00 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testWriteCVOpsLongAddr() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 12;
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.writeCVOpsMode(CV1, val2, p3, 4 * 128 + 0x23, true);
        Assert.assertEquals("write message",
                "EF 0E 7C 67 00 04 23 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    public void testWriteCVOpsShortAddr() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager(lnis);
        int CV1 = 12;
        int val2 = 34;
        ProgListener p3 = null;
        slotmanager.writeCVOpsMode(CV1, val2, p3, 22, false);
        Assert.assertEquals("write message",
                "EF 0E 7C 67 00 00 16 00 00 0B 22 7F 7F 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SlotManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SlotManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    LocoNetInterfaceScaffold lnis;

    protected void setUp() {
        jmri.util.JUnitUtil.resetInstanceManager();
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
