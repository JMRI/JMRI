package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BlockOrderTest {

    OBlockManager _blkMgr;

    @Test
    public void testCTor() {
        BlockOrder t = new BlockOrder(new OBlock("OB1", "Test"));
        Assert.assertNotNull("exists",t);
    }

    /* for the sake of code coverage */
    @Test
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
    @Test
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        _blkMgr = new OBlockManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockOrderTest.class);

}
