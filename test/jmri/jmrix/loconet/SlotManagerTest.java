
package jmri.jmrix.loconet;

import jmri.ProgListener;
import jmri.Programmer;
import junit.framework.Assert;
import junit.framework.TestCase;


public class SlotManagerTest extends TestCase {

    public SlotManagerTest(String s) {
	super(s);
    }

    /**
     * Local member to recall when a SlotListener has been invoked.
     */
    private LocoNetSlot testSlot;

    public void testGetSlotSend()  {
        SlotManager slotmanager = new SlotManager();
        testSlot = null;
        SlotListener p2=  new SlotListener(){
            public void notifyChangedSlot(LocoNetSlot l) {
                testSlot = l;
            }
        };
        slotmanager.slotFromLocoAddress(0x2134, p2);
        Assert.assertEquals("slot request message",
			    "BF 42 34 00",
			    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
        Assert.assertEquals("hash length", 1, slotmanager.mLocoAddrHash.size());
        Assert.assertEquals("key present", true,
            slotmanager.mLocoAddrHash.containsKey(new Integer(0x2134)));
        Assert.assertEquals("value present", true,
            slotmanager.mLocoAddrHash.contains(p2));
    }

    public void testGetSlotRcv() {
        SlotManager slotmanager = new SlotManager();
        testSlot = null;
        SlotListener p2=  new SlotListener(){
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
        m2.setElement(1, 0xE );
        m2.setElement(2, 0xB );
        m2.setElement(3, 3 );
        m2.setElement(4, 0x34 );
        m2.setElement(5, 0 );
        m2.setElement(6, 0 );
        m2.setElement(7, 4 );
        m2.setElement(8, 0 );
        m2.setElement(9, 0x42 );
        m2.setElement(10, 0 );
        m2.setElement(11, 0 );
        m2.setElement(12, 0 );
        m2.setElement(13, 0x6c );
        slotmanager.message(m2);
        Assert.assertEquals("returned slot", slotmanager.slot(11), testSlot);
        // and make sure it forgets
        testSlot = null;
        slotmanager.message(m1);
        slotmanager.message(m2);
        Assert.assertEquals("returned slot", null, testSlot);
     }

    public void testReadCVPaged() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        ProgListener p2=  null;
        slotmanager.setMode(Programmer.PAGEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
			    "EF 0E 7C 20 00 00 00 00 02 0B 7F 00 00 00",
			    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testReadCVRegister() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  2;
        ProgListener p2=  null;
        slotmanager.setMode(Programmer.REGISTERMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
			    "EF 0E 7C 10 00 00 00 00 02 01 7F 00 00 00",
			    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testReadCVDirect() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        ProgListener p2=  null;
        slotmanager.setMode(Programmer.DIRECTBYTEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
			    "EF 0E 7C 28 00 00 00 00 02 0B 7F 00 00 00", lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testReadCVOpsModeLong() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        ProgListener p2=  null;
        slotmanager.readCVOpsMode(CV1, p2, 4*128+0x23, true);
        Assert.assertEquals("read message",
			    "EF 0E 7C 2F 00 04 23 00 02 0B 7F 00 00 00", lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testReadCVOpsModeShort() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        ProgListener p2=  null;
        slotmanager.readCVOpsMode(CV1, p2, 22, false);
        Assert.assertEquals("read message",
			    "EF 0E 7C 2F 00 00 16 00 02 0B 7F 00 00 00", lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testWriteCVPaged() throws jmri.ProgrammerException {
	SlotManager slotmanager = new SlotManager();
	int CV1=  12;
	int val2=  34;
        ProgListener p3=  null;
	slotmanager.setMode(Programmer.PAGEMODE);
        slotmanager.writeCV(CV1, val2, p3);
	Assert.assertEquals("write message",
			    "EF 0E 7C 60 00 00 00 00 00 0B 22 00 00 00",
			    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testWriteCVRegister() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  2;
        int val2=  34;
        ProgListener p3=  null;
        slotmanager.setMode(Programmer.REGISTERMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
			    "EF 0E 7C 50 00 00 00 00 00 01 22 00 00 00",
                            lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testWriteCVDirect() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        int val2=  34;
        ProgListener p3=  null;
        slotmanager.setMode(Programmer.DIRECTBYTEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
			    "EF 0E 7C 68 00 00 00 00 00 0B 22 00 00 00",
			    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testWriteCVOpsLongAddr() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        int val2=  34;
        ProgListener p3=  null;
        slotmanager.writeCVOpsMode(CV1, val2, p3, 4*128+0x23, true);
        Assert.assertEquals("write message",
			    "EF 0E 7C 67 00 04 23 00 00 0B 22 00 00 00",
			    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testWriteCVOpsShortAddr() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        int val2=  34;
        ProgListener p3=  null;
        slotmanager.writeCVOpsMode(CV1, val2, p3, 22, false);
        Assert.assertEquals("write message",
			    "EF 0E 7C 67 00 00 16 00 00 0B 22 00 00 00",
			    lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    LocoNetInterfaceScaffold lnis;
    protected void setUp() {
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        log4jfixtureInst.setUp();
    }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
