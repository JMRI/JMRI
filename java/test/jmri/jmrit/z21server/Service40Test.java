package jmri.jmrit.z21server;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ControllerFilterAction
 *
 * @author Eckart Meyer (C) 2025
 */
public class Service40Test {

    @Test
    public void testCtor() {
        Service40 obj = new Service40();
        Assert.assertNotNull("exists", obj);
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
