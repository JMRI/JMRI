package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.SignalHead;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BlockOrderTest extends TestCase {

    OBlockManager _blkMgr;

    public void testCTor() {
        BlockOrder t = new BlockOrder(new OBlock("OB1", "Test"));
        Assert.assertNotNull("exists",t);
    }

    /* for the sake of code coverage */
    public void testGettersAndSetters() {
        OBlock b = _blkMgr.provideOBlock("OB1");
        BlockOrder bo = new BlockOrder(b, "foo", "foo", "foo");
        Assert.assertNotNull("exists", bo);
        bo.setEntryName("entryP");
        Assert.assertEquals("Portal name", "entryP", bo.getEntryName());
        bo.setExitName("exitP");
        Assert.assertEquals("Portal name", "exitP", bo.getExitName());
        bo.setPathName("Path");
        Assert.assertEquals("Path name", "Path", bo.getPathName());
        OBlock bb = _blkMgr.provideOBlock("OB2");
        bo.setBlock(bb);
        Assert.assertEquals("Block bb", bb, bo.getBlock());
    }

    /* tests OBlocks more than BlockOrders,
     *  but adds to coverage if tested here.
     */
    public void testBlockMembers() {
        OBlock block = _blkMgr.provideOBlock("OB1");
        PortalManager portalMgr = InstanceManager.getDefault(PortalManager.class);
        Portal portal = portalMgr.providePortal("foop");
        portal.setToBlock(block, true);
        SignalHead sh = new jmri.implementation.VirtualSignalHead("IH1", "sig1"); 
        Assert.assertNotNull("signal", sh);
        sh.setAppearance(SignalHead.YELLOW);
        OPath p = new OPath(block, "path");
        portal.setProtectSignal(sh, 20, block);
        block.addPath(p);
        block.addPortal(portal);
        BlockOrder bo = new BlockOrder(block, "path", "foop", null);
        Assert.assertEquals("OPath", p, bo.getPath());
        Assert.assertEquals("Entry portal", portal, bo.getEntryPortal());
        Assert.assertNull("Exit portal", bo.getExitPortal());
        Assert.assertEquals("signal", sh, bo.getSignal());
        Assert.assertEquals("Entrance speedType", "Medium", bo.getPermissibleEntranceSpeed());
        Assert.assertEquals(20, bo.getEntranceSpace(), 0);
    }

    // from here down is testing infrastructure
    public BlockOrderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", BlockOrderTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(BlockOrderTest.class);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        _blkMgr = new OBlockManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(BlockOrderTest.class.getName());

}
