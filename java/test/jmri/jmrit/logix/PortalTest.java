package jmri.jmrit.logix;

import jmri.Turnout;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 *
 */
public class PortalTest extends TestCase {

    OBlockManager _blkMgr;
    PortalManager _portalMgr;
    jmri.TurnoutManager _turnoutMgr;

    public void testCtor() {
        Portal p = _portalMgr.createNewPortal("IP1", null);
        Assert.assertNull("No User Name", p);       // Portals must have a user name
        p = _portalMgr.createNewPortal(null, "portal_1");
        Assert.assertNotNull("Has User Name", p);
    }
    
    public void testValidPortal() {
        Portal p = _portalMgr.providePortal("portal_2");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        OBlock fromBlk = _blkMgr.provideOBlock("OB2");
        p.setToBlock(toBlk, false);
        Assert.assertFalse("from block null", p.isValid());
        p.setFromBlock(toBlk, false);
        Assert.assertFalse("toBlock = fromBlock", p.isValid());
        p.setFromBlock(fromBlk, true);
        Assert.assertTrue("portal has both blocks", p.isValid());
    }

    public void testAddPath() {
        Portal p = _portalMgr.providePortal("portal_3");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        OBlock fromBlk = _blkMgr.provideOBlock("OB2");
        p.setToBlock(toBlk, false);
        p.setFromBlock(fromBlk, true);
        Assert.assertTrue("portal has both blocks", p.isValid());
        OPath path1 = new OPath("path_1", fromBlk, p, null, null);
        OPath path2 = new OPath("path_2", toBlk, null, p, null);
        OPath path3 = new OPath("path_3", toBlk, null, p, null);
        Assert.assertTrue("Add path_1 to fromBlk", fromBlk.addPath(path1));
        Assert.assertFalse("Cannot Add path_1 to toBlk", toBlk.addPath(path1));
        Assert.assertTrue("Add path_2 to toBlk", toBlk.addPath(path2));
        Assert.assertFalse("Add path_3 to portal", p.addPath(path3));    // path already in list with name "path_2"
        Assert.assertEquals(path2, path3);      // no distinguishing feature - name difference insufficient.
        Assert.assertEquals("Number of toPaths", 1, p.getToPaths().size());
        Turnout to = _turnoutMgr.provideTurnout("turnout_1");
        path2.addSetting(new jmri.BeanSetting(to, Turnout.CLOSED));
        path3.addSetting(new jmri.BeanSetting(to, Turnout.THROWN));
        Assert.assertTrue("Add path_3 to portal", p.addPath(path3));
        Assert.assertEquals("Number of toPaths", 2, p.getToPaths().size());
        Assert.assertEquals("Number of fromPaths", 1, p.getFromPaths().size());
        OPath path2B = new OPath("path_2", toBlk, null, p, null);
        to = _turnoutMgr.provideTurnout("turnout_2");
        path2B.addSetting(new jmri.BeanSetting(to, Turnout.CLOSED));
        Assert.assertFalse("Add path_2B to portal", p.addPath(path2B));
        Assert.assertEquals("Number of toPaths", 2, p.getToPaths().size());
        path2B.setName("path_2B");      // changing name updates portals
        Assert.assertEquals("Number of toPaths", 3, p.getToPaths().size());
        Assert.assertTrue("Add path_2B to portal", p.addPath(path2B));
        Assert.assertEquals("Number of toPaths", 3, p.getToPaths().size());
        
        p.removePath(path2);
        Assert.assertEquals("Number of toPaths", 2, p.getToPaths().size());
        Assert.assertEquals("Number of fromPaths", 1, p.getFromPaths().size());
        
        jmri.util.JUnitAppender.assertWarnMessage("Path path_1 already in block OB2, cannot be added to block OB1");
        jmri.util.JUnitAppender.assertWarnMessage("Path \"path_3\" is duplicate of path \"path_2\" in Portal \"portal_3\" from block OB1.");
        jmri.util.JUnitAppender.assertWarnMessage("Path \"path_2\" is duplicate name for another path in Portal \"portal_3\" from block OB1.");
    }

    // from here down is testing infrastructure
    public PortalTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PortalTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(PortalTest.class);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        _blkMgr = new OBlockManager();
        _portalMgr = new PortalManager();
        _turnoutMgr = jmri.InstanceManager.turnoutManagerInstance();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
