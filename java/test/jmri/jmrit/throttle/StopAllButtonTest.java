package jmri.jmrit.throttle;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of StopAllButton
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class StopAllButtonTest {

    @Test
    public void testCtor() {
        StopAllButton panel = new StopAllButton();
        Assert.assertNotNull("exists", panel);
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }
}
