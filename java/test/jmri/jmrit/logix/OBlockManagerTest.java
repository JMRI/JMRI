package jmri.jmrit.logix;

import jmri.Block;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the OBlockManager class.
 *
 * @author Bob Coleman Copyright 2012
 * @author Bob Jacobsen Copyright 2014
 */
public class OBlockManagerTest {

    OBlockManager l;
    
    @Test
    public void testProvide() {
        // original create with systemname
        OBlock b1 = l.provide("OB101");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "OB101", b1.getSystemName());
    }

    @Test
    public void testProvideWorksTwice() {
        Block b1 = l.provide("OB102");
        Block b2 = l.provide("OB102");
        Assert.assertNotNull(b1);
        Assert.assertNotNull(b2);
        Assert.assertEquals(b1, b2);
    }

    @Test
    public void testProvideFailure() {
        boolean correct = false;
        try {
            l.provide("");
            Assert.fail("didn't throw");
        } catch (IllegalArgumentException ex) {
            correct = true;
        }
        Assert.assertTrue("Exception thrown properly", correct);     
    }
    
    @Test
    public void testCreateNewOBlock() {
        Assert.assertNull("createNewOBlock", l.createNewOBlock("", "user"));
        Assert.assertNull("createNewOBlock", l.createNewOBlock("OB", "user"));
    }

    @Test
    public void testGetOBlock() {
        // the is was originally part of Warrant test, but none of the asserts
        // are testing anything in the warrant.
        OBlockManager _OBlockMgr = InstanceManager.getDefault(OBlockManager.class);
        OBlock bWest = _OBlockMgr.createNewOBlock("OB1", "West");
        OBlock bEast = _OBlockMgr.createNewOBlock("OB2", "East");
        OBlock bNorth = _OBlockMgr.createNewOBlock("OB3", "North");
        OBlock bSouth = _OBlockMgr.createNewOBlock("OB4", "South");
        Assert.assertEquals("OBlock", bWest, _OBlockMgr.getOBlock("West"));
        Assert.assertEquals("OBlock", bEast, _OBlockMgr.getOBlock("OB2"));
        Assert.assertEquals("OBlock", bNorth, _OBlockMgr.getOBlock("North"));
        Assert.assertEquals("OBlock", bSouth, _OBlockMgr.getOBlock("OB4"));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        l = new OBlockManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
