package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ThrottlesListAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ThrottlesListActionTest {

    @Test
    public void testCtor() {
        ThrottlesListAction panel = new ThrottlesListAction();
        Assert.assertNotNull("exists", panel);
        
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
