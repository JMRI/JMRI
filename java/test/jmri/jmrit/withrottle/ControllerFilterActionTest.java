package jmri.jmrit.withrottle;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ControllerFilterAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ControllerFilterActionTest {

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        ControllerFilterAction panel = new ControllerFilterAction();
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
