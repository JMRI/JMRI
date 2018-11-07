package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of WiThrottleManager
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class WiThrottleManagerTest {

    @Test
    public void testCtor() {
        WiThrottleManager panel = new WiThrottleManager();
        Assert.assertNotNull("exists", panel );
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }
    
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
