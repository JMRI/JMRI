package jmri.jmrit.withrottle;

import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.consisttool.TestConsistManager;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Test simple functioning of ConsistController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ConsistControllerTest extends TestCase {

    public void testCtor() {
        ConsistController panel = new ConsistController();
        Assert.assertNotNull("exists", panel );
    }

    // from here down is testing infrastructure
    public ConsistControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ConsistControllerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConsistControllerTest.class);
        return suite;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        InstanceManager.setDefault(ConsistManager.class, new TestConsistManager());
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }
    
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        JUnitUtil.tearDown();
    }
}
