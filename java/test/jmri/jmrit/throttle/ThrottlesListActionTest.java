package jmri.jmrit.throttle;

import jmri.util.JUnitUtil;

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
        Assertions.assertNotNull( panel, "exists");

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
