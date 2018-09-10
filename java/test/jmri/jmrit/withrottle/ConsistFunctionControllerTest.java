package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of ConsistFunctionController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ConsistFunctionControllerTest extends TestCase {

    public void testCtor() {
        ThrottleController tc = new ThrottleController();
        ConsistFunctionController panel = new ConsistFunctionController(tc);
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public ConsistFunctionControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ConsistFunctionControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConsistFunctionControllerTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        jmri.util.JUnitUtil.setUp();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        jmri.util.JUnitUtil.tearDown();

    }
}
