package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ThrottlesTableModel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesTableModelTest {

    @Test
    public void testCtor() {
        ThrottlesTableModel panel = new ThrottlesTableModel();
        Assert.assertNotNull("exists", panel);
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
