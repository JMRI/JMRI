
package jmri.jmrix.loconet;

import junit.framework.*;
import jmri.*;



public class LnOpsModeProgrammerTest extends TestCase {
    
    public LnOpsModeProgrammerTest(String s) {
	super(s);
    }
    
    public void testSetMode() {
	SlotManager val1=  new SlotManager();
	LnOpsModeProgrammer lops = new LnOpsModeProgrammer(val1, 1, true);
	lops.setMode(Programmer.PAGEMODE);
	Assert.assertEquals("did not go to page mode", 0, lops.getMode());
	lops.setMode(Programmer.REGISTERMODE);
	Assert.assertEquals("did not go to register mode", 0, lops.getMode());
	//lops.setMode(Programmer.OPSMODE);
	//Assert.assertEquals("did go to ops mode", 0, lops.getMode());
	
    }
    public void testWriteCV() {
	SlotManager val1=  null  /** @todo fill in non-null value */;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, 1, true);
	int CV1=  0;
	int val2=  0;
	ProgListener p3=  null  /** @todo fill in non-null value */;
	try {
	    lnopsmodeprogrammer.writeCV(CV1, val2, p3);
	    /** @todo:  Insert test code here.  Use assertEquals(), for example. */
	}
	catch(Exception e) {
	    System.err.println("Exception thrown:  "+e);
	}
    }
    public void testReadCV() {
	SlotManager val1=  null  /** @todo fill in non-null value */;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1,1, true);
	int CV1=  0;
	ProgListener p2=  null  /** @todo fill in non-null value */;
	try {
	    lnopsmodeprogrammer.readCV(CV1, p2);
	    /** @todo:  Insert test code here.  Use assertEquals(), for example. */
	}
	catch(Exception e) {
	    System.err.println("Exception thrown:  "+e);
	}
    }
    public void testGetMode() {
	SlotManager val1=  null  /** @todo fill in non-null value */;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1, 1, true);
	int intRet = lnopsmodeprogrammer.getMode();
	/** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }
    public void testGetCanRead() {
	SlotManager val1=  new SlotManager();
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1,1, true);
	Assert.assertEquals("ops mode cant yet read", false, 
			    lnopsmodeprogrammer.getCanRead());
    }
    public void testConfirmCV() {
	SlotManager val1=  null  /** @todo fill in non-null value */;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1,1, true);
	int CV1=  0;
	int val2=  0;
	ProgListener p3=  null  /** @todo fill in non-null value */;
	try {
	    lnopsmodeprogrammer.confirmCV(CV1, val2, p3);
	    /** @todo:  Insert test code here.  Use assertEquals(), for example. */
	}
	catch(Exception e) {
	    System.err.println("Exception thrown:  "+e);
	}
    }
    public void testHasMode() {
	SlotManager val1=  null  /** @todo fill in non-null value */;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1,1,true);
	int mode1=  0;
	boolean booleanRet = lnopsmodeprogrammer.hasMode(mode1);
	/** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }
    
    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }
}
