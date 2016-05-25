package jmri.jmrit.consisttool;

import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test simple functioning of ConsistToolFrame
 *
 * @author	Paul Bender Copyright (C) 2015
 * @version	$Revision$
 */
public class ConsistToolFrameTest extends TestCase {

    public void testCtor() {
        jmri.InstanceManager.setDefault(jmri.ConsistManager.class,new TestConsistManager());
        ConsistToolFrame frame = new ConsistToolFrame();
        Assert.assertNotNull("exists", frame );
    }

    // from here down is testing infrastructure
    public ConsistToolFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ConsistToolFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConsistToolFrameTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
