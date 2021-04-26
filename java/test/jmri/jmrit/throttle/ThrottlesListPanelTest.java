package jmri.jmrit.throttle;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ThrottlesListPanel
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottlesListPanelTest {

    @Test
    public void testCtor() {
        ThrottlesListPanel panel = new ThrottlesListPanel();
        Assert.assertNotNull("exists", panel);
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }
}
