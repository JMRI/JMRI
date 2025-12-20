package jmri.jmrit.z21server;

import jmri.util.JUnitUtil;

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
        Assertions.assertNotNull( obj, "exists");
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
