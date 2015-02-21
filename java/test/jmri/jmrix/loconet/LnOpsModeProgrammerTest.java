package jmri.jmrix.loconet;

import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;
import junit.framework.Assert;
import junit.framework.TestCase;

public class LnOpsModeProgrammerTest extends TestCase {

    public LnOpsModeProgrammerTest(String s) {
        super(s);
    }

    public void testSetMode() {
        SlotManager val1 = null;
        LnOpsModeProgrammer lops = new LnOpsModeProgrammer(val1, 1, true);
        try {
            lops.setMode(DefaultProgrammerManager.PAGEMODE);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("No IllegalArgumentException thrown");

    }

    public void testGetMode() {
        SlotManager val1 = null;
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, 1, true);
        ProgrammingMode intRet = lnopsmodeprogrammer.getMode();
        Assert.assertEquals("OpsByteMode", DefaultProgrammerManager.OPSBYTEMODE, intRet);
    }

    public void testGetCanRead() {
        SlotManager val1 = null;
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, 1, true);
        Assert.assertEquals("ops mode always can read", true,
                lnopsmodeprogrammer.getCanRead());
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
