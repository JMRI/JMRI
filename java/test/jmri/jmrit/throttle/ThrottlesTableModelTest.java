package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
