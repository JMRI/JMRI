package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.implementation.VirtualSignalHead;
import jmri.implementation.VirtualSignalMast;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the Portal class.
 */
public class PortalTest {

    private OBlockManager _blkMgr;
    private PortalManager _portalMgr;
    private jmri.TurnoutManager _turnoutMgr;

    @Test
    public void testCtor() {
        Portal p = null;
//        try {
//            p = _portalMgr.createNewPortal(null); // annotated as nonnull so should not be tested
//        } catch (NullPointerException ex) {
//            // expected: "Name cannot be null"
//        }
//        assertThat(p).withFailMessage("Null User Name").isNull();
        p = _portalMgr.createNewPortal("");
        assertThat(p).withFailMessage("Empty User Name").isNull(); // Portals must have a user name
        p = _portalMgr.createNewPortal("portal_1");
        assertThat(p).withFailMessage("Has User Name").isNotNull();
    }
    
    @Test
    public void testValidPortal() {
        Portal p = _portalMgr.providePortal("portal_2");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        OBlock fromBlk = _blkMgr.provideOBlock("OB2");
        p.setToBlock(toBlk, false);
        assertThat(p.isValid()).withFailMessage("from block null").isFalse();
        p.setFromBlock(toBlk, false);
        assertThat(p.isValid()).withFailMessage("toBlock = fromBlock").isFalse();
        p.setFromBlock(fromBlk, true);
        assertThat(p.isValid()).withFailMessage("portal has both blocks").isTrue();
    }

    @Test
    public void testAddPath() {
        Portal p = _portalMgr.providePortal("portal_3");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        OBlock fromBlk = _blkMgr.provideOBlock("OB2");
        p.setToBlock(toBlk, false);
        p.setFromBlock(fromBlk, true);
        assertThat(p.isValid()).withFailMessage("portal has both blocks").isTrue();
        OPath path1 = new OPath("path_1", fromBlk, p, null, null);
        OPath path2 = new OPath("path_2", toBlk, null, p, null);
        OPath path3 = new OPath("path_3", toBlk, null, p, null);
        assertThat(fromBlk.addPath(path1)).withFailMessage("Add path_1 to fromBlk").isTrue();
        assertThat(toBlk.addPath(path1)).withFailMessage("Cannot Add path_1 to toBlk").isFalse();
        assertThat(toBlk.addPath(path2)).withFailMessage("Add path_2 to toBlk").isTrue();
        assertThat(p.addPath(path3)).withFailMessage("Add path_3 to portal").isFalse();    // path already in list with name "path_2"
        assertThat(path3).isEqualTo(path2);      // no distinguishing feature - name difference insufficient.
        assertThat(p.getToPaths().size()).withFailMessage("Number of toPaths").isEqualTo(1);
        Turnout to = _turnoutMgr.provideTurnout("turnout_1");
        path2.addSetting(new jmri.BeanSetting(to, Turnout.CLOSED));
        path3.addSetting(new jmri.BeanSetting(to, Turnout.THROWN));
        assertThat(p.addPath(path3)).withFailMessage("Add path_3 to portal").isTrue();
        assertThat(p.getToPaths().size()).withFailMessage("Number of toPaths").isEqualTo(2);
        assertThat(p.getFromPaths().size()).withFailMessage("Number of fromPaths").isEqualTo(1);
        OPath path2B = new OPath("path_2", toBlk, null, p, null);
        to = _turnoutMgr.provideTurnout("turnout_2");
        path2B.addSetting(new jmri.BeanSetting(to, Turnout.CLOSED));
        assertThat(p.addPath(path2B)).withFailMessage("Add path_2B to portal").isFalse();
        assertThat(p.getToPaths().size()).withFailMessage("Number of toPaths").isEqualTo(2);
        path2B.setName("path_2B");      // changing name updates portals
        assertThat(p.getToPaths().size()).withFailMessage("Number of toPaths").isEqualTo(3);
        assertThat(p.addPath(path2B)).withFailMessage("Add path_2B to portal").isTrue();
        assertThat(p.getToPaths().size()).withFailMessage("Number of toPaths").isEqualTo(3);
        
        p.removePath(path2);
        assertThat(p.getToPaths().size()).withFailMessage("Number of toPaths").isEqualTo(2);
        assertThat(p.getFromPaths().size()).withFailMessage("Number of fromPaths").isEqualTo(1);
        
        JUnitAppender.assertWarnMessage("Path \"path_1\" already in block OB2, cannot be added to block OB1");
        JUnitAppender.assertWarnMessage("Path \"path_3\" is duplicate of path \"path_2\" in Portal \"portal_3\" from block OB1.");
        JUnitAppender.assertWarnMessage("Path \"path_2\" is duplicate name for another path in Portal \"portal_3\" from block OB1.");

        // now that we have at least one path set up, test method on those paths
        assertThat(p.getPathsWithinBlock(toBlk).size()).withFailMessage("toBlk path list size").isEqualTo(2);
    }

    @Test
    public void testSetProtectSignal() {
        Portal p = _portalMgr.providePortal("portal_3");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        OBlock fromBlk = _blkMgr.provideOBlock("OB2");
        p.setToBlock(toBlk, false);
        p.setFromBlock(fromBlk, true);
        VirtualSignalHead sh1 = new VirtualSignalHead("IH1");
        assertThat(p.setProtectSignal(sh1, 200, null)).withFailMessage("null protectedBlock").isFalse();
        p.setProtectSignal(sh1,200, toBlk);
        assertThat(p.getSignalProtectingBlock(toBlk)).withFailMessage("portal has signal").isNotNull();
        VirtualSignalHead sh2 = new VirtualSignalHead("IH2");
        p.setProtectSignal(sh2,200, fromBlk);
        assertThat(p.getSignalProtectingBlock(fromBlk)).withFailMessage("portal has signal").isNotNull();
        assertThat(p.setProtectSignal(sh2, 100, _blkMgr.provideOBlock("OB3"))).withFailMessage("set signal with wrong block").isFalse();

        // a (static) method in Portal acting on signals
        assertThat(Portal.getSignal("IH1")).withFailMessage("get signal head").isNull(); // would not expect null
        assertThat(p.getProtectedBlock(sh1).getDisplayName()).withFailMessage("block protected by IH1").isEqualTo("OB1");
        p.deleteSignal(sh1);
        assertThat(p.getFromSignal()).withFailMessage("ToSignal deleted from portal").isNull();
    }

    @Test
    public void testSetName() {
        Portal p = _portalMgr.providePortal("portal_1");
        Portal p3 = _portalMgr.providePortal("portal_3");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        p.setFromBlock(toBlk, false);
        p3.setToBlock(toBlk, false);
        assertThat(p.setName(null)).withFailMessage("portal set empty").isNull();
        assertThat(p.setName("portal_1")).withFailMessage("portal set new name").isNull(); // set old name
        assertThat(p.setName("portal_2")).withFailMessage("portal set new name").isNull();
        assertThat(p.getName()).withFailMessage("portal get new name").isEqualTo("portal_2");
        assertThat(p.setName("portal_3")).withFailMessage("portal setName returned Error message").isNotNull();
    }

    @Test
    public void testGetPermissibleSpeed() {
        Portal p = _portalMgr.providePortal("portal_1");
        OBlock toBlk = _blkMgr.provideOBlock("OB1");
        OBlock fromBlk = _blkMgr.provideOBlock("OB2");
        p.setToBlock(toBlk, false);
        p.setFromBlock(fromBlk, true);
        assertThat(p.getPermissibleSpeed(toBlk, false)).withFailMessage("block exitSpeed not set").isNull();
        // signal head
        SignalHead sh1 = new VirtualSignalHead("IH1");
        p.setProtectSignal(sh1, 200, fromBlk);
        p.getPermissibleSpeed(fromBlk, false);
        JUnitAppender.assertErrorMessageStartsWith("SignalHead \"IH1\" has no exit speed specified for appearance \"Dark\"!");
        sh1.setAppearance(SignalHead.RED);
        assertThat(p.getPermissibleSpeed(fromBlk, true)).withFailMessage("get protecting signal speed").isEqualTo("Stop");
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
        assertThat(p.getDescription()).withFailMessage("portal description").isEqualTo("Portal \"portal p\" between OBlocks \"OB2\" and \"OB1\"");
        p.dispose();
        assertThat(_portalMgr.getPortal("portal_1")).withFailMessage("portal p disposed").isNull();
    }

    // from here down is testing infrastructure
    // setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();        
        _blkMgr = InstanceManager.getDefault(OBlockManager.class);
        _portalMgr = InstanceManager.getDefault(PortalManager.class);
        _turnoutMgr = jmri.InstanceManager.turnoutManagerInstance();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
