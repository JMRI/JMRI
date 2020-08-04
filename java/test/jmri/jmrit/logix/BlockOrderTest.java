package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BlockOrderTest {

    OBlockManager _blkMgr;

    @Test
    public void testCTor() {
        BlockOrder t = new BlockOrder(new OBlock("OB1", "Test"));
        assertThat(t).withFailMessage("exists").isNotNull();
    }

    /* for the sake of code coverage */
    @Test
    public void testGettersAndSetters() {
        OBlock b = _blkMgr.provideOBlock("OB1");
        BlockOrder bo = new BlockOrder(b, "foo", "foo", "foo");
        assertThat(bo).withFailMessage("exists").isNotNull();
        bo.setEntryName("entryP");
        assertThat(bo.getEntryName()).withFailMessage("Portal name").isEqualTo("entryP");
        bo.setExitName("exitP");
        assertThat(bo.getExitName()).withFailMessage("Portal name").isEqualTo("exitP");
        bo.setPathName("Path");
        assertThat(bo.getPathName()).withFailMessage("Path name").isEqualTo("Path");
        OBlock bb = _blkMgr.provideOBlock("OB2");
        bo.setBlock(bb);
        assertThat(bo.getBlock()).withFailMessage("Block bb").isEqualTo(bb);
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
        assertThat(sh).withFailMessage("signal").isNotNull();
        sh.setAppearance(SignalHead.YELLOW);
        OPath p = new OPath(block, "path");
        portal.setProtectSignal(sh, 20, block);
        block.addPath(p);
        block.addPortal(portal);
        BlockOrder bo = new BlockOrder(block, "path", "foop", null);
        assertThat(bo.getPath()).withFailMessage("OPath").isEqualTo(p);
        assertThat(bo.getEntryPortal()).withFailMessage("Entry portal").isEqualTo(portal);
        assertThat(bo.getExitPortal()).withFailMessage("Exit portal").isNull();
        assertThat(bo.getSignal()).withFailMessage("signal").isEqualTo(sh);
        assertThat(bo.getPermissibleEntranceSpeed()).withFailMessage("Entrance speedType").isEqualTo("Medium");
        assertThat(bo.getEntranceSpace()).isEqualTo(20.0f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();        _blkMgr = new OBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BlockOrderTest.class);

}
