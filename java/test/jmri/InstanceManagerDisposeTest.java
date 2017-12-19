package jmri;

import jmri.util.JUnitUtil;
// import junit.framework.Test;
// import junit.framework.TestCase;
// import junit.framework.TestSuite;
//import org.junit.Assert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test InstanceManager
 */
public class InstanceManagerDisposeTest {

    // Test that an object that is added to the InstanceManager and then
    // removed is also disposed.
    
    static boolean b = false;
    
    public static class DisposableClass implements Disposable {
        @Override
        public void dispose() {
            b = true;
        }
    }
    
    @Test
    public void testClear() {
        
        DisposableClass disposable = new DisposableClass();

        InstanceManager.store(disposable, DisposableClass.class);
        InstanceManager.getDefault().clear(DisposableClass.class);

        Assert.assertTrue("the disposable has been disposed", b);
    }

    // from here down is testing infrastructure

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(InstanceManagerDisposeTest.class);
}
