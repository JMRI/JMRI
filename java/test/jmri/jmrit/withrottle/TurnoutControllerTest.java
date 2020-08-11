package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TurnoutController
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TurnoutControllerTest {

    @Test
    public void testCtor() {
        TurnoutController panel = new TurnoutController();
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
