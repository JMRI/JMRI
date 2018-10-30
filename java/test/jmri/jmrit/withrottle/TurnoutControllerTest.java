package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of TurnoutController
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TurnoutControllerTest {

    @Test
    public void testCtor() {
        TurnoutController panel = new TurnoutController();
        Assert.assertNotNull("exists", panel );
    }

    @After
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }
    
    @Before
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
