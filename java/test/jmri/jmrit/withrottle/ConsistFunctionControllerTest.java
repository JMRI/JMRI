package jmri.jmrit.withrottle;

import jmri.InstanceManager;
import jmri.NamedBeanHandleManager;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ConsistFunctionController
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConsistFunctionControllerTest {

    @Test
    public void testCtor() {
        ThrottleController tc = new ThrottleController();
        ConsistFunctionController panel = new ConsistFunctionController(tc);
        Assert.assertNotNull("exists", panel );
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        InstanceManager.setDefault(NamedBeanHandleManager.class, new NamedBeanHandleManager());
    }
    
    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
