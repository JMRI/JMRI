package jmri.jmrit.logix;

import jmri.Block;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the OBlockManager class
 * <P>
 * @author Bob Coleman Copyright 2012
 * @author Bob Jacobsen Copyright 2014
 */
public class OBlockManagerTest {

    OBlockManager l;
    
    @Test
    public void testProvide() {
        // original create with systemname
        OBlock b1 = l.provideOBlock("OB101");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "OB101", b1.getSystemName());
    }

    @Test
    public void testProvideWorksTwice() {
        Block b1 = l.provideOBlock("OB102");
        Block b2 = l.provideOBlock("OB102");
        Assert.assertNotNull(b1);
        Assert.assertNotNull(b2);
        Assert.assertEquals(b1, b2);
    }

    @Test
    public void testProvideFailure() {
        boolean correct = false;
        try {
            l.provideOBlock("");
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
