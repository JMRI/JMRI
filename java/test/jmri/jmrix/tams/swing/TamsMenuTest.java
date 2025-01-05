package jmri.jmrix.tams.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TamsMenu
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TamsMenuTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtor() {
        TamsMenu action = new TamsMenu(new jmri.jmrix.tams.TamsSystemConnectionMemo());
        Assertions.assertNotNull( action, "exists");
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
