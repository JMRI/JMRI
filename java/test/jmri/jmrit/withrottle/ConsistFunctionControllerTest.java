package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;
import org.junit.*;

/**
 * Test simple functioning of ConsistFunctionController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ConsistFunctionControllerTest {

    @Test
    public void testCtor() {
        ThrottleController tc = new ThrottleController();
        ConsistFunctionController panel = new ConsistFunctionController(tc);
        Assert.assertNotNull("exists", panel );
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }
    
    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
