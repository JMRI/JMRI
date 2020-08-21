package jmri.jmrit.logix;

import java.util.ArrayList;

import jmri.BeanSetting;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the OPath class.
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class OPathTest {
    
    OBlockManager _blkMgr;
    PortalManager _portalMgr;
    jmri.TurnoutManager _turnoutMgr;

    @Test
    public void testCtor() {
        Block b = new Block("IB1");

        OPath op = new OPath(b, "name");

        assertThat(op.getName()).withFailMessage("name").isEqualTo("name");
        assertThat(op.getBlock()).withFailMessage("block").isEqualTo(b);
    }

    @Test
    public void testNullBlockCtor() {

        OPath op = new OPath(null, "name");

        assertThat(op.getName()).withFailMessage("name").isEqualTo("name");
        assertThat(op.getBlock()).withFailMessage("block").isEqualTo(null);
    }

    @Test
    public void testSetBlockNonNull() {
        Block b1 = new Block("IB1");
        Block b2 = new Block("IB2");

        OPath op = new OPath(b1, "name");
        op.setBlock(b2);

        assertThat(op.getBlock()).withFailMessage("block").isEqualTo(b2);
    }

    @Test
    public void testSetBlockWasNull() {
        Block b = new Block("IB1");

        OPath op = new OPath(null, "name");
        op.setBlock(b);

        assertThat(op.getBlock()).withFailMessage("block").isEqualTo(b);
    }

    @Test
    public void testSetBlockToNull() {
        Block b1 = new Block("IB1");

        OPath op = new OPath(b1, "name");
        op.setBlock(null);

        assertThat(op.getBlock()).withFailMessage("block").isEqualTo(null);
    }

    public void testSetOBlockToNull() {
        OBlock b1 = new OBlock("IB1");

        OPath op = new OPath(b1, "name");
        op.setBlock(null);

        assertThat(op.getBlock()).withFailMessage("block").isEqualTo(null);
    }

    @Test
    @SuppressWarnings("unlikely-arg-type") // String seems to be unrelated to OPath
    public void testEquals() {
        Block b1 = new Block("IB1");

        OPath op1 = new OPath(b1, "name");
        op1.setBlock(null);
        OPath op2 = new OPath(b1, "name");
        op2.setBlock(null);
        
        Assert.assertFalse("not equals null", op1.equals(null));
        Assert.assertFalse("not equals string", op1.equals(""));
        
        assertThat(op1.equals(op1)).withFailMessage("equals self").isTrue();
        assertThat(op1.equals(op2)).withFailMessage("on contents").isTrue();
    }
    
    @Test
    public void testPortals() {
        Portal entryP = _portalMgr.providePortal("entryP");
        Portal exitP = _portalMgr.providePortal("exitP");
        OBlock blk = _blkMgr.provideOBlock("OB0");
        ArrayList<BeanSetting> ats = new ArrayList<BeanSetting>();
        OPath path = new OPath("path", blk, entryP, exitP, ats);
        assertThat(path.getFromPortal()).withFailMessage("Get entry portal").isEqualTo(entryP);
        assertThat(path.getToPortal()).withFailMessage("Get exit portal").isEqualTo(exitP);
    }
    
    @Test
    public void testNameChange() {
        Portal exitP = _portalMgr.providePortal("exitP");
        OBlock blk = _blkMgr.provideOBlock("OB0");
        Turnout to = _turnoutMgr.provideTurnout("turnout_1");
        OPath path = new OPath("path", blk, null, null, null);
        path.setToPortal(exitP);
        path.addSetting(new jmri.BeanSetting(to, Turnout.CLOSED));
        assertThat(path.getToPortal()).withFailMessage("Get exit portal").isEqualTo(exitP);
        path.setName("OtherPath");
        assertThat(path.getName()).withFailMessage("path name change").isEqualTo("OtherPath");
        assertThat(to.getCommandedState()).withFailMessage("turnout unknown").isEqualTo(Turnout.UNKNOWN);
        path.setTurnouts(0, true, 0, false);
        assertThat(to.getCommandedState()).withFailMessage("path name change").isEqualTo(Turnout.CLOSED);
    }
    
    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        _blkMgr = new OBlockManager();
        _portalMgr = InstanceManager.getDefault(PortalManager.class);
        _turnoutMgr = jmri.InstanceManager.turnoutManagerInstance();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
