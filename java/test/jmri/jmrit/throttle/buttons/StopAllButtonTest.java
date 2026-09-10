package jmri.jmrit.throttle.buttons;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 * @author Steve Young Copyright (C)2026
 */
@DisabledIfHeadless
public class StopAllButtonTest {

    @Test
    void testCtor() {
        StopAllButton t = new StopAllButton();
        assertNotNull(t);
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
