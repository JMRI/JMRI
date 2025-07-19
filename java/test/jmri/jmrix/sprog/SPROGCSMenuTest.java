package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SPROGCSMenu.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SPROGCSMenuTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtor() {
        SPROGCSMenu action = new SPROGCSMenu(new jmri.jmrix.sprog.SprogSystemConnectionMemo());
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
