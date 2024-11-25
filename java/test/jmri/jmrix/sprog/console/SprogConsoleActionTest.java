package jmri.jmrix.sprog.console;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SprogConsoleAction.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SprogConsoleActionTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtor() {
        SprogConsoleAction action = new SprogConsoleAction("SPROG Action Test", new jmri.jmrix.sprog.SprogSystemConnectionMemo());
        Assertions.assertNotNull( action, "exists");
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
