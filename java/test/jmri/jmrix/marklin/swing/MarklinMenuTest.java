package jmri.jmrix.marklin.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MarklinMenu
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MarklinMenuTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtor() {
        var memo = new jmri.jmrix.marklin.MarklinSystemConnectionMemo();
        MarklinMenu action = new MarklinMenu(memo);
        Assertions.assertNotNull( action, "exists");
        memo.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
