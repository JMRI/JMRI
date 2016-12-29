package jmri.jmrit.logix;

import jmri.Block;
import jmri.Turnout;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the OPath class
 *
 * @author	Bob Jacobsen Copyright 2010
 */
public class OPathTest extends TestCase {
    
    OBlockManager _blkMgr;
    PortalManager _portalMgr;
    jmri.TurnoutManager _turnoutMgr;

    public void testCtor() {
        Block b = new Block("IB1");

        OPath op = new OPath(b, "name");

        Assert.assertEquals("name", "name", op.getName());
        Assert.assertEquals("block", b, op.getBlock());
    }

    public void testNullBlockCtor() {

        OPath op = new OPath(null, "name");

        Assert.assertEquals("name", "name", op.getName());
        Assert.assertEquals("block", null, op.getBlock());
    }

    public void testSetBlockNonNull() {
        Block b1 = new Block("IB1");
        Block b2 = new Block("IB2");

        OPath op = new OPath(b1, "name");
        op.setBlock(b2);

        Assert.assertEquals("block", b2, op.getBlock());
    }

    public void testSetBlockWasNull() {
        Block b = new Block("IB1");

        OPath op = new OPath(null, "name");
        op.setBlock(b);

        Assert.assertEquals("block", b, op.getBlock());
    }

    public void testSetBlockToNull() {
        Block b1 = new Block("IB1");

        OPath op = new OPath(b1, "name");
        op.setBlock(null);

        Assert.assertEquals("block", null, op.getBlock());
    }

    public void testEquals() {
        Block b1 = new Block("IB1");

        OPath op1 = new OPath(b1, "name");
        op1.setBlock(null);
        OPath op2 = new OPath(b1, "name");
        op2.setBlock(null);
        
        Assert.assertFalse("not equals null", op1.equals(null));
        Assert.assertFalse("not equals string", op1.equals(""));
        
        Assert.assertTrue("equals self", op1.equals(op1));
        Assert.assertTrue("on contents", op1.equals(op2));
    }
    
    public void testPortals() {
        Portal entryP = _portalMgr.providePortal("entryP");
        Portal exitP = _portalMgr.providePortal("exitP");
        OBlock blk = _blkMgr.provideOBlock("OB0");
        OPath path = new OPath("path", blk, entryP, exitP, null);
        Assert.assertEquals("Get entry portal", entryP, path.getFromPortal());
        Assert.assertEquals("Get exit portal", exitP, path.getToPortal());
    }
    
    public void testNameChange() {
        Portal exitP = _portalMgr.providePortal("exitP");
        OBlock blk = _blkMgr.provideOBlock("OB0");
        Turnout to = _turnoutMgr.provideTurnout("turnout_1");
        OPath path = new OPath("path", blk, null, null, null);
        path.setToPortal(exitP);
        path.addSetting(new jmri.BeanSetting(to, Turnout.CLOSED));
        Assert.assertEquals("Get exit portal", exitP, path.getToPortal());
        path.setName("OtherPath");
        Assert.assertEquals("path name change", "OtherPath", path.getName());
        Assert.assertEquals("turnout unknown", Turnout.UNKNOWN, to.getCommandedState());
        path.setTurnouts(0, true, 0, false);
        Assert.assertEquals("path name change", Turnout.CLOSED, to.getCommandedState());
    }
    
    // from here down is testing infrastructure
    public OPathTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OPathTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(OPathTest.class);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        _blkMgr = new OBlockManager();
        _portalMgr = new PortalManager();
        _turnoutMgr = jmri.InstanceManager.turnoutManagerInstance();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
