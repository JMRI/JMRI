
package jmri.jmrix.loconet;

import junit.framework.*;
import jmri.*;



public class LnOpsModeProgrammerTest extends TestCase {

    public LnOpsModeProgrammerTest(String s) {
        super(s);
    }

    public void testSetMode() {
        SlotManager val1=  null;
        LnOpsModeProgrammer lops = new LnOpsModeProgrammer(val1, 1, true){
                void reportBadMode(int i){}
            };
        try {
            lops.setMode(ProgrammingMode.PAGEMODE);
        } catch (IllegalArgumentException e) { return; }
        Assert.fail("No IllegalArgumentException thrown");

    }
    public void testGetMode() {
        SlotManager val1=  null;
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, 1, true);
        ProgrammingMode intRet = lnopsmodeprogrammer.getMode();
        Assert.assertEquals("OpsByteMode", ProgrammingMode.OPSBYTEMODE, intRet);
    }
    public void testGetCanRead() {
        SlotManager val1=  null;
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1,1, true);
        Assert.assertEquals("ops mode always can read", true,
                    lnopsmodeprogrammer.getCanRead());
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
