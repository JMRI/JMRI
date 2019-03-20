package jmri.jmris.simpleserver;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerManager class 
 *
 * @author Paul Bender
 */
public class SimpleServerManagerTest {

    @Test
    public void testGetInstance() {
        SimpleServerManager a = SimpleServerManager.getInstance();
        Assert.assertNotNull(a);
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }

}
