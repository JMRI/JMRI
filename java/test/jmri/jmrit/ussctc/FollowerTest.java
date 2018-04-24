package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

/**
 * Tests for Follower classes in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class FollowerTest extends TestCase {

    public void testCreate() {
        Follower f = new Follower("12", "34", false, "56");
        
        Assert.assertEquals("12", f.getOutputName());
        Assert.assertEquals("34", f.getSensorName());
        Assert.assertEquals(false, f.getInvert());
        Assert.assertEquals("56", f.getVetoName());
    }

    public void testInstantiate() {
        Follower f = new Follower("12", "34", false, "56");
        f.instantiate();        
    }

    public void testCreateRep() throws jmri.JmriException {
        JUnitUtil.initRouteManager();
        Follower f = new Follower("12", "34", false, "56");
        f.instantiate();
        new Follower("12");
    }

    // from here down is testing infrastructure
    public FollowerTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    public void setUp() {
        JUnitUtil.setUp();
    }

    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {FollowerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FollowerTest.class);
        return suite;
    }

}
