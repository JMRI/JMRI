
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
	lops.setMode(Programmer.PAGEMODE);
	Assert.assertEquals("did not go to page mode", Programmer.OPSBYTEMODE, lops.getMode());
	lops.setMode(Programmer.REGISTERMODE);
	Assert.assertEquals("did not go to register mode", Programmer.OPSBYTEMODE, lops.getMode());

    }
    public void testGetMode() {
	SlotManager val1=  null;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, 1, true);
	int intRet = lnopsmodeprogrammer.getMode();
	Assert.assertEquals("OpsByteMode", Programmer.OPSBYTEMODE, intRet);
    }
    public void testGetCanRead() {
	SlotManager val1=  null;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1,1, true);
	Assert.assertEquals("ops mode always can read", true,
			    lnopsmodeprogrammer.getCanRead());
    }
    public void testHasMode() {
	SlotManager val1=  null;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1,1,true);
	Assert.assertEquals("Ops byte mode", true, lnopsmodeprogrammer.hasMode(Programmer.OPSBYTEMODE));
	Assert.assertEquals("Paged mode", false, lnopsmodeprogrammer.hasMode(Programmer.PAGEMODE));
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
