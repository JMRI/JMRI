package jmri.jmrit.z21server;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of ControllerFilterAction
 *
 * @author Eckart Meyer (C) 2025
 */
public class NumberMapActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NumberMapActionTest panel = new NumberMapActionTest();
        Assert.assertNotNull("exists", panel);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
