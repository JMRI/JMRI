package jmri.jmrit.timetable.swing;

import org.junit.*;

/**
 * Tests for the Bundle Class
 * @author Dave Sand Copyright (C) 2018
 */
public class BundleTest {

    @Test
    public void testGoodKeyMessage() {
        Assert.assertEquals("Open Timetable", Bundle.getMessage("TimeTableAction"));  // NOI18N
    }

    @Test
    public void testBadKeyMessage() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT");  // NOI18N
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    @Test
    public void testGoodKeyMessageArg() {
        Assert.assertEquals("Open Timetable", Bundle.getMessage("TimeTableAction", new Object[]{}));  // NOI18N
        Assert.assertEquals("One -- Two", Bundle.getMessage("LabelTrain", "One", "Two"));  // NOI18N
    }

    @Test
    public void testBadKeyMessageArg() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT", new Object[]{});  // NOI18N
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");  // NOI18N
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