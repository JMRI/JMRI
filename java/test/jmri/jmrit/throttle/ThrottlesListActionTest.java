package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottlesListAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesListActionTest {

    @Test
    public void testCtor() {
        ThrottlesListAction panel = new ThrottlesListAction();
        Assert.assertNotNull("exists", panel);
        
    }

    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
