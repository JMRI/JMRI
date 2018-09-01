package jmri.jmrit.withrottle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of TrackPowerController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TrackPowerControllerTest extends TestCase {

    public void testCtor() {
        TrackPowerController panel = new TrackPowerController();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public TrackPowerControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TrackPowerControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TrackPowerControllerTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        jmri.util.JUnitUtil.setUp();

    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        jmri.util.JUnitUtil.tearDown();

    }
}
