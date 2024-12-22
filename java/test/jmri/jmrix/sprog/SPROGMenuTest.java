package jmri.jmrix.sprog;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SPROGMenu.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SPROGMenuTest {

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    public void testCtor() {
        SPROGMenu action = new SPROGMenu(new jmri.jmrix.sprog.SprogSystemConnectionMemo());
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
