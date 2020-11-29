package jmri.jmrit.logix;

import jmri.Block;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(b1.getSystemName()).withFailMessage("system name").isEqualTo("OB101");
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
        assertThat(correct).withFailMessage("Exception thrown properly").isTrue();
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
        assertThat(_OBlockMgr.getOBlock("West")).withFailMessage("OBlock").isEqualTo(bWest);
        assertThat(_OBlockMgr.getOBlock("OB2")).withFailMessage("OBlock").isEqualTo(bEast);
        assertThat(_OBlockMgr.getOBlock("North")).withFailMessage("OBlock").isEqualTo(bNorth);
        assertThat(_OBlockMgr.getOBlock("OB4")).withFailMessage("OBlock").isEqualTo(bSouth);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();        l = new OBlockManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
