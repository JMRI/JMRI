package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottleController
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottleControllerTest {

    @Test
    public void testCtor() {
        ThrottleController panel = new ThrottleController();
        Assert.assertNotNull("exists", panel );
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
