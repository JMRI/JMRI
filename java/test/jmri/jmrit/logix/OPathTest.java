package jmri.jmrit.logix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import jmri.BeanSetting;
import jmri.Block;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the OPath class.
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class OPathTest {
    
    private OBlockManager _blkMgr;
    private PortalManager _portalMgr;
    private jmri.TurnoutManager _turnoutMgr;

    @Test
    public void testCtor() {
        Block b = new Block("IB1");

        OPath op = new OPath(b, "name");

        assertEquals( "name", op.getName(), "name");
        assertEquals( b, op.getBlock(),"block");
    }

    @Test
    public void testNullBlockCtor() {

        OPath op = new OPath(null, "name");

        assertEquals( "name", op.getName(), "name");
        assertNull(op.getBlock(), "block");
    }

    @Test
    public void testSetBlockNonNull() {
        Block b1 = new Block("IB1");
        Block b2 = new Block("IB2");

        OPath op = new OPath(b1, "name");
        op.setBlock(b2);

        assertEquals( b2, op.getBlock(), "block");
    }

    @Test
    public void testSetBlockWasNull() {
        Block b = new Block("IB1");

        OPath op = new OPath(null, "name");
        op.setBlock(b);

        assertEquals( b, op.getBlock(), "block");
    }

    @Test
    public void testSetBlockToNull() {
        Block b1 = new Block("IB1");

        OPath op = new OPath(b1, "name");
        op.setBlock(null);

        assertNull( op.getBlock(), "block");
    }

    @Test
    public void testSetOBlockToNull() {
        OBlock b1 = new OBlock("IB1");

        OPath op = new OPath(b1, "name");
        op.setBlock(null);

        assertNull( op.getBlock(), "block");
    }

    @Test
    @SuppressWarnings({"unlikely-arg-type", "IncompatibleEquals", "ObjectEqualsNull"}) // String seems to be unrelated to OPath
    public void testEquals() {
        Block b1 = new Block("IB1");

        OPath op1 = new OPath(b1, "name");
        op1.setBlock(null);
        OPath op2 = new OPath(b1, "name");
        op2.setBlock(null);
        
        assertFalse( op1.equals(null), "not equals null");
        assertFalse( op1.equals(""), "not equals string");
        
        assertTrue( op1.equals(op1), "equals self");
        assertTrue(op1.equals(op2), "on contents");
    }
    
    @Test
    public void testPortals() {
        Portal entryP = _portalMgr.providePortal("entryP");
        Portal exitP = _portalMgr.providePortal("exitP");
        OBlock blk = _blkMgr.provideOBlock("OB0");
        ArrayList<BeanSetting> ats = new ArrayList<>();
        OPath path = new OPath("path", blk, entryP, exitP, ats);
        assertEquals( entryP, path.getFromPortal(), "Get entry portal");
        assertEquals( exitP, path.getToPortal(), "Get exit portal");
    }
    
    @Test
    public void testNameChange() {
        Portal exitP = _portalMgr.providePortal("exitP");
        OBlock blk = _blkMgr.provideOBlock("OB0");
        Turnout to = _turnoutMgr.provideTurnout("turnout_1");
        OPath path = new OPath("path", blk, null, null, null);
        path.setToPortal(exitP);
        path.addSetting(new jmri.BeanSetting(to, Turnout.CLOSED));
        assertEquals( exitP, path.getToPortal(), "Get exit portal");
        path.setName("OtherPath");
        assertEquals( "OtherPath", path.getName(), "path name change");
        assertEquals( Turnout.UNKNOWN, to.getCommandedState(), "turnout unknown");
        path.setTurnouts(0, true, 0, false);
        assertEquals( Turnout.CLOSED, to.getCommandedState(), "path name change");
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
