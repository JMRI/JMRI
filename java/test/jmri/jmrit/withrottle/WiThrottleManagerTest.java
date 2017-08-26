package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of WiThrottleManager
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiThrottleManagerTest extends TestCase {

    public void testCtor() {
        WiThrottleManager panel = new WiThrottleManager();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public WiThrottleManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", WiThrottleManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(WiThrottleManagerTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        JUnitUtil.tearDown();
    }
}
