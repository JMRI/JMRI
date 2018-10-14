package jmri.jmrit.timetable.swing;

import org.junit.*;

/**
 * Tests for the Bundle Class
 * @author Dave Sand Copyright (C) 2018
 */
public class BundleTest {

    @Test
    public void testCreate() {
        new Bundle();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}