
package jmri.jmrix.loconet;

import junit.framework.*;
import jmri.*;



public class SlotManagerTest extends TestCase {

    public SlotManagerTest(String s) {
        super(s);
    }

    public void testReadCVPaged() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        ProgListener p2=  null;
        slotmanager.setMode(Programmer.PAGEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "ef e 7c 20 0 0 0 0 2 b 7f 0 0 0 ",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testReadCVRegister() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  2;
        ProgListener p2=  null;
        slotmanager.setMode(Programmer.REGISTERMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "ef e 7c 10 0 0 0 0 2 1 7f 0 0 0 ",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testReadCVDirect() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        ProgListener p2=  null;
        slotmanager.setMode(Programmer.DIRECTBYTEMODE);
        slotmanager.readCV(CV1, p2);
        Assert.assertEquals("read message",
                "ef e 7c 28 0 0 0 0 2 b 7f 0 0 0 ", lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testReadCVOpsModeLong() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        ProgListener p2=  null;
        slotmanager.readCVOpsMode(CV1, p2, 4*128+0x23, true);
        Assert.assertEquals("read message",
                "ef e 7c 2c 0 4 23 0 2 b 7f 0 0 0 ", lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testReadCVOpsModeShort() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        ProgListener p2=  null;
        slotmanager.readCVOpsMode(CV1, p2, 77, false);
        Assert.assertEquals("read message",
                "b0 14 10 0 ", lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testWriteCVPaged() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        int val2=  34;
        ProgListener p3=  null;
        slotmanager.setMode(Programmer.PAGEMODE);
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "ef e 7c 60 0 0 0 0 0 b 22 0 0 0 ",
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
                "ef e 7c 60 0 0 0 0 0 b 22 0 0 0 ",
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
                "ef e 7c 60 0 0 0 0 0 b 22 0 0 0 ",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testWriteCVOpsLongAddr() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        int val2=  34;
        ProgListener p3=  null;
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "ef e 7c 60 0 0 0 0 0 b 22 0 0 0 ",
                lnis.outbound.elementAt(lnis.outbound.size()-1).toString());
    }

    public void testWriteCVOpsShortAddr() throws jmri.ProgrammerException {
        SlotManager slotmanager = new SlotManager();
        int CV1=  12;
        int val2=  34;
        ProgListener p3=  null;
        slotmanager.writeCV(CV1, val2, p3);
        Assert.assertEquals("write message",
                "ef e 7c 60 0 0 0 0 0 b 22 0 0 0 ",
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
