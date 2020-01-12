package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of RouteController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class RouteControllerTest {

    @Test
    public void testCtor() {
        RouteController panel = new RouteController();
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
