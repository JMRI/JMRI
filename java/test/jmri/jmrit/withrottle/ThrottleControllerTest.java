package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of ThrottleController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottleControllerTest {

    @Test
    public void testCtor() {
        ThrottleController panel = new ThrottleController();
        Assert.assertNotNull("exists", panel );
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }
    
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
