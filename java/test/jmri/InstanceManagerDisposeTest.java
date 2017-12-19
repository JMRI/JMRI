package jmri;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test InstanceManager
 */
public class InstanceManagerDisposeTest extends TestCase implements InstanceManagerAutoDefault {

    // Test that an object that is added to the InstanceManager and then
    // removed is also disposed.
    
    static boolean b = false;
    
    public static class DisposableClass implements Disposable {
        @Override
        public void dispose() {
            b = true;
        }
    }
    
    public void testClear() {
        
        DisposableClass disposable = new DisposableClass();

        InstanceManager.store(disposable, DisposableClass.class);
        InstanceManager.getDefault().clear(DisposableClass.class);

        Assert.assertTrue("the disposable has been disposed", b);
    }

    // from here down is testing infrastructure
    public InstanceManagerDisposeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {InstanceManagerDisposeTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(InstanceManagerDisposeTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(InstanceManagerDisposeTest.class);
}
