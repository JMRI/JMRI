package jmri.jmrix.loconet;

import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;
import junit.framework.Assert;
import junit.framework.TestCase;

public class LnOpsModeProgrammerTest extends TestCase {

    public void testSetMode() {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager val1 = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, val1);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, memo, 1, true);

        try {
            lnopsmodeprogrammer.setMode(DefaultProgrammerManager.PAGEMODE);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("No IllegalArgumentException thrown");

    }

    public void testGetMode() {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager val1 = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, val1);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, memo, 1, true);

        ProgrammingMode intRet = lnopsmodeprogrammer.getMode();
        Assert.assertEquals("OpsByteMode", DefaultProgrammerManager.OPSBYTEMODE, intRet);
    }

    public void testGetCanRead() {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager val1 = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, val1);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, memo, 1, true);

        Assert.assertEquals("ops mode always can read", true,
                lnopsmodeprogrammer.getCanRead());
    }

    public void testSV2DataBytes() {
        LocoNetMessage m = new LocoNetMessage(15);

        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager val1 = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, val1);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, memo, 1, true);

        // check data bytes
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x12345678);
        Assert.assertEquals(0x10, m.getElement(10));
        Assert.assertEquals(0x78, m.getElement(11));
        Assert.assertEquals(0x56, m.getElement(12));
        Assert.assertEquals(0x34, m.getElement(13));
        Assert.assertEquals(0x12, m.getElement(14));
    }
    
    public void testSV2highBits() {
        LocoNetMessage m = new LocoNetMessage(15);

        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager val1 = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis, val1);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, memo, 1, true);

        // check high bits
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01020384);
        Assert.assertEquals(0x11, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01028304);
        Assert.assertEquals(0x12, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01820304);
        Assert.assertEquals(0x14, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x81020304);
        Assert.assertEquals(0x18, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x81828384);
        Assert.assertEquals(0x1F, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));
    }
    
    // from here down is testing infrastructure
    public LnOpsModeProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LnOpsModeProgrammerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
