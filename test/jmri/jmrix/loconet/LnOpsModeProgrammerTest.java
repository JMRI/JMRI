
package jmri.jmrix.loconet;

import junit.framework.*;
import jmri.*;



public class LnOpsModeProgrammerTest extends TestCase {
    
    public LnOpsModeProgrammerTest(String s) {
	super(s);
    }
    
    public void testSetMode() {
	SlotManager val1=  new SlotManager();
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1);
	int mode1=  0;
	lnopsmodeprogrammer.setMode(mode1);
	/** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }
    public void testWriteCV() {
	SlotManager val1=  null  /** @todo fill in non-null value */;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1);
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
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1);
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
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1);
	int intRet = lnopsmodeprogrammer.getMode();
	/** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }
    public void testGetCanRead() {
	SlotManager val1=  null  /** @todo fill in non-null value */;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1);
	boolean booleanRet = lnopsmodeprogrammer.getCanRead();
	/** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }
    public void testConfirmCV() {
	SlotManager val1=  null  /** @todo fill in non-null value */;
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1);
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
	LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(val1);
	int mode1=  0;
	boolean booleanRet = lnopsmodeprogrammer.hasMode(mode1);
	/** @todo:  Insert test code here.  Use assertEquals(), for example. */
    }
    
    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }
}
