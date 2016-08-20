package jmri.jmrit.logix;

import jmri.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the OBlockManager class
 * <P>
 * @author Bob Coleman Copyright 2012
 * @author Bob Jacobsen Copyright 2014
 */
public class OBlockManagerTest extends TestCase {

    OBlockManager l;
    
    public void testProvide() {
        // original create with systemname
        OBlock b1 = l.provideOBlock("OB101");
        Assert.assertNotNull(b1);
        Assert.assertEquals("system name", "OB101", b1.getSystemName());
    }

    public void testProvideWorksTwice() {
        Block b1 = l.provideOBlock("OB102");
        Block b2 = l.provideOBlock("OB102");
        Assert.assertNotNull(b1);
        Assert.assertNotNull(b2);
        Assert.assertEquals(b1, b2);
    }

    public void testProvideFailure() {
        boolean correct = false;
        try {
            OBlock t = l.provideOBlock("");
            Assert.fail("didn't throw");
        } catch (IllegalArgumentException ex) {
            correct = true;
        }
        Assert.assertTrue("Exception thrown properly", correct);
        
    }

    // from here down is testing infrastructure
    public OBlockManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OBlockManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OBlockManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        l = new OBlockManager();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
