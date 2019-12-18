package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.VirtualSignalHead;
import jmri.implementation.VirtualSignalMast;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Portal class.
 */
public class PortalTest {

    private OBlockManager _blkMgr;
    private PortalManager _portalMgr;
    private jmri.TurnoutManager _turnoutMgr;

    @Test
    public void testCtor() {
        Portal p = _portalMgr.createNewPortal(null);
        Assert.assertNull("No User Name", p);       // Portals must have a user name
        p = _portalMgr.createNewPortal("portal_1");
        Assert.assertNotNull("Has User Name", p);
    }
    
    @Test
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

    @Test
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
        
        JUnitAppender.assertWarnMessage("Path \"path_1\" already in block OB2, cannot be added to block OB1");
        JUnitAppender.assertWarnMessage("Path \"path_3\" is duplicate of path \"path_2\" in Portal \"portal_3\" from block OB1.");
        JUnitAppender.assertWarnMessage("Path \"path_2\" is duplicate name for another path in Portal \"portal_3\" from block OB1.");

        // now that we have at least one path set up, test method on those paths
        Assert.assertEquals("toBlk path list size", 2, p.getPathsWithinBlock(toBlk).size());
    }

    @Test
    public void testSetProtectSignal() {
        Portal p = _portalMgr.providePortal("portal_3");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        OBlock fromBlk = _blkMgr.provideOBlock("OB2");
        p.setToBlock(toBlk, false);
        p.setFromBlock(fromBlk, true);
        VirtualSignalHead sh1 = new VirtualSignalHead("IH1");
        Assert.assertFalse("null protectedBlock", p.setProtectSignal(sh1, 200, null));
        p.setProtectSignal(sh1,200, toBlk);
        Assert.assertNotNull("portal has signal", p.getSignalProtectingBlock(toBlk));
        VirtualSignalHead sh2 = new VirtualSignalHead("IH2");
        p.setProtectSignal(sh2,200, fromBlk);
        Assert.assertNotNull("portal has signal", p.getSignalProtectingBlock(fromBlk));
        Assert.assertFalse("set signal with wrong block",
                p.setProtectSignal(sh2, 100, _blkMgr.provideOBlock("OB3")));

        // a (static) method in Portal acting on signals
        Assert.assertNull("get signal head", Portal.getSignal("IH1")); // would not expect null
        Assert.assertEquals("block protected by IH1", "OB1", p.getProtectedBlock(sh1).getDisplayName());
        p.deleteSignal(sh1);
        Assert.assertNull("ToSignal deleted from portal", p.getFromSignal());
    }

    @Test
    public void testSetName() {
        Portal p = _portalMgr.providePortal("portal_1");
        Portal p3 = _portalMgr.providePortal("portal_3");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        p.setFromBlock(toBlk, false);
        p3.setToBlock(toBlk, false);
        Assert.assertNull("portal set empty", p.setName(null));
        Assert.assertNull("portal set new name", p.setName("portal_1")); // set old name
        Assert.assertNull("portal set new name", p.setName("portal_2"));
        Assert.assertEquals("portal get new name", "portal_2", p.getName());
        Assert.assertNotNull("portal setName returned Error message", p.setName("portal_3"));
    }

    @Test
    public void testGetPermissibleSpeed() {
        Portal p = _portalMgr.providePortal("portal_1");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        OBlock fromBlk = _blkMgr.provideOBlock("OB2");
        p.setToBlock(toBlk, false);
        p.setFromBlock(fromBlk, true);
        Assert.assertNull("block exitSpeed not set", p.getPermissibleSpeed(toBlk, false));
        // signal head
        SignalHead sh1 = new VirtualSignalHead("IH1");
        p.setProtectSignal(sh1, 200, fromBlk);
        p.getPermissibleSpeed(fromBlk, false);
        JUnitAppender.assertErrorMessageStartsWith("SignalHead \"IH1\" has no exit speed specified for appearance \"Dark\"!");
        sh1.setAppearance(SignalHead.RED);
        Assert.assertEquals("get protecting signal speed", "Stop", p.getPermissibleSpeed(fromBlk, true));
        // signal mast
        VirtualSignalMast sm1 = new VirtualSignalMast("IF$vsm:basic:one-searchlight($1)", "mast 1");
        p.setProtectSignal(sm1, 90, toBlk);
        p.getPermissibleSpeed(toBlk, true);
        JUnitAppender.assertErrorMessageStartsWith("SignalMast \"mast 1\" has no entrance speed specified for aspect \"null\"!");
    }

    @Test
    public void testDisposePortal() {
        Portal p = _portalMgr.providePortal("portal p");
        p.setToBlock(_blkMgr.provideOBlock("OB1"), false);
        p.setFromBlock(_blkMgr.provideOBlock("OB2"), true);
        Assert.assertEquals("portal description",
                "Portal \"portal p\" between OBlocks \"OB2\" and \"OB1\"", p.getDescription());
        p.dispose();
        Assert.assertNull("portal p disposed", _portalMgr.getPortal("portal_1"));
    }

    // from here down is testing infrastructure
    // setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        
        _blkMgr = InstanceManager.getDefault(OBlockManager.class);
        _portalMgr = InstanceManager.getDefault(PortalManager.class);
        _turnoutMgr = jmri.InstanceManager.turnoutManagerInstance();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
