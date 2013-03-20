// BlockManagerTest.java

package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

//TODO: Bob C says: Needed for the commented out Default Speed tests
/*
import java.util.logging.Level;
import java.util.logging.Logger;
import jmri.JmriException;
*/

/**
 * Tests for the BlockManager class
 * <P>
 * Based upon a stub by Bob Jacobsen Copyright (C) 2006 
 * <P>
 * @author Bob Coleman Copyright 2012
 * @version $Revision$
 */
public class BlockManagerTest extends TestCase {

    public void testCreate1() {
        // original create with systemname and username
        Block b1 = InstanceManager.blockManagerInstance().createNewBlock("SystemName1","UserName1");
        Assert.assertEquals("system name", "SYSTEMNAME1", b1.getSystemName());
        Assert.assertEquals("user name", "UserName1", b1.getUserName());
    }

    public void testCreate2() {
        // original create with systemname and empty username
        Block b1 = InstanceManager.blockManagerInstance().createNewBlock("SystemName2","");
        Assert.assertEquals("system name", "SYSTEMNAME2", b1.getSystemName());
        Assert.assertEquals("user name", "", b1.getUserName());
    }

    public void testCreate3() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.blockManagerInstance().createNewBlock("UserName3");
        Assert.assertEquals("system name", "IB:AUTO:0001", b1.getSystemName());
        Assert.assertEquals("user name", "UserName3", b1.getUserName());
    }

    public void testCreate4() {
        // original create with no systemname and an empty username
        Block b1 = InstanceManager.blockManagerInstance().createNewBlock("");
        Assert.assertEquals("system name", "IB:AUTO:0002", b1.getSystemName());
        Assert.assertEquals("user name", "", b1.getUserName());
    }

    public void testGet1() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.blockManagerInstance().createNewBlock("UserName4");
        Assert.assertEquals("system name", "IB:AUTO:0003", b1.getSystemName());
        Assert.assertEquals("user name", "UserName4", b1.getUserName());

        Block bget1 = InstanceManager.blockManagerInstance().getBlock("UserName4");
        Assert.assertEquals("get system name by user name", "IB:AUTO:0003", bget1.getSystemName());
        Assert.assertEquals("get user name by user name", "UserName4", bget1.getUserName());

        Block bget2 = InstanceManager.blockManagerInstance().getBlock("IB:AUTO:0003");
        Assert.assertEquals("get system name by system name", "IB:AUTO:0003", bget2.getSystemName());
        Assert.assertEquals("get user name by system name", "UserName4", bget2.getUserName());
    }

    public void testProvide1() {
        // original create with no systemname and a username
        Block b1 = InstanceManager.blockManagerInstance().createNewBlock("UserName5");
        Assert.assertEquals("system name", "IB:AUTO:0004", b1.getSystemName());
        Assert.assertEquals("user name", "UserName5", b1.getUserName());

        Block bprovide1 = InstanceManager.blockManagerInstance().provideBlock("UserName5");
        Assert.assertEquals("provide system name by user name", "IB:AUTO:0004", bprovide1.getSystemName());
        Assert.assertEquals("provide user name by user name", "UserName5", bprovide1.getUserName());

        Block bprovide2 = InstanceManager.blockManagerInstance().provideBlock("IB:AUTO:0004");
        Assert.assertEquals("provide system name by system name", "IB:AUTO:0004", bprovide2.getSystemName());
        Assert.assertEquals("provide user name by system name", "UserName5", bprovide2.getUserName());

        // auto create with prefixed systemname and no username
        Block bprovide3 = InstanceManager.blockManagerInstance().provideBlock("SystemName6");
        Assert.assertEquals("provide system name by user name", "IBSYSTEMNAME6", bprovide3.getSystemName());
        Assert.assertEquals("provide user name by user name", null, bprovide3.getUserName());

        // auto create with accepted systemname and no username
        Block bprovide4 = InstanceManager.blockManagerInstance().provideBlock("IB:AUTO:0005");
        Assert.assertEquals("provide system name by system name", "IB:AUTO:0005", bprovide4.getSystemName());
        Assert.assertEquals("provide user name by system name", null, bprovide4.getUserName());
    }

    public void testAutoSkip1() {
        // following test should skip IB:AUTO:0006
        Block bautoskip1 = InstanceManager.blockManagerInstance().createNewBlock("IB:AUTO:0007","UserName7");
        Assert.assertEquals("autoskip system name", "IB:AUTO:0007", bautoskip1.getSystemName());
        Assert.assertEquals("autoskip user name", "UserName7", bautoskip1.getUserName());

        Block bautoskip2 = InstanceManager.blockManagerInstance().createNewBlock("UserName8");
        Assert.assertEquals("autoskip system name skip", "IB:AUTO:0008", bautoskip2.getSystemName());
        Assert.assertEquals("autoskip user name skip", "UserName8", bautoskip2.getUserName());
    }

    public void testBlockSpeed1() {
        Block bspeed1 = InstanceManager.blockManagerInstance().createNewBlock("UserName9");
        Assert.assertEquals("block speed system name", "IB:AUTO:0009", bspeed1.getSystemName());
        Assert.assertEquals("block speed user name", "UserName9", bspeed1.getUserName());
        Assert.assertEquals("block speed", "Use Global Normal", bspeed1.getBlockSpeed());
    }

//TODO: Bob C says: The following test fails -- InstanceManager not set up correctly -- I bet
/*
    public void testDefaultSpeed1() {
        Assert.assertEquals("default block speed", "Normal", InstanceManager.blockManagerInstance().getDefaultSpeed());

        try {
            InstanceManager.blockManagerInstance().setDefaultSpeed("Faster");
        } catch (JmriException ex) {
            Logger.getLogger(BlockManagerXmlTest.class.getName()).log(Level.SEVERE, null, ex);
        }
//TODO: Bob C says: The following test fails -- InstanceManager not set up correctly -- I bet
        Assert.assertEquals("faster block speed", "Faster", InstanceManager.blockManagerInstance().getDefaultSpeed());

        try {
            InstanceManager.blockManagerInstance().setDefaultSpeed("Normal");
        } catch (JmriException ex) {
            Logger.getLogger(BlockManagerXmlTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        Assert.assertEquals("block speed back to normal", "Normal", InstanceManager.blockManagerInstance().getDefaultSpeed());
    }
*/
    
/**
 * The following was here and will be removed shortly:
 * <P>
 * This is the stub by Bob Jacobsen Copyright (C) 2006 
 */
    /**
     * Normally, users create Block objects via a manager, 
     * but we test the direct create here.  If it works, we can 
     * use it for testing.
     */
	public void testDirectCreate() {
	    new Block("SystemName");
	}

/* 	public void testShortDelay() { */
/* 		SimpleTimebase p = new SimpleTimebase(); */
/* 		Date now = new Date(); */
/* 		p.setTime(now); */
/* 		p.setRate(100.); */
/* 		wait(100); */
/* 		Date then = p.getTime(); */
/* 		long delta = then.getTime()-now.getTime(); */
/* 		Assert.assertTrue("delta ge 50 (nominal value)", delta>=50); */
/* 		Assert.assertTrue("delta lt 150 (nominal value)", delta<150); */
/* 	} */

	// from here down is testing infrastructure

	public BlockManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {BlockManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(BlockManagerTest.class);
		return suite;
	}

//TODO: Bob C says: Needed for the commented out Default Speed tests
/*
        static Logger log = Logger.getLogger(BlockManagerXmlTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
*/
}
