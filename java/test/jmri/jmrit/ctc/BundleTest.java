package jmri.jmrit.ctc;

import java.util.Locale;
import org.junit.*;

/**
 * Tests for the Bundle Class
 * @author Dave Sand Copyright (C) 2019
 */
public class BundleTest {

    @Test
    public void testGoodKeyMessage() {
        Assert.assertEquals("Start CTC Runtime", Bundle.getMessage("CtcRunAction"));  // NOI18N
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
        Assert.assertEquals("Start CTC Runtime", Bundle.getMessage("CtcRunAction", new Object[]{}));  // NOI18N
//         Assert.assertEquals("One -- Two", Bundle.getMessage("LabelTrain", "One", "Two"));  // NOI18N
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

    @Test public void testLocaleMessage() {
        Assert.assertEquals("Hintergrund:", Bundle.getMessage(Locale.GERMANY, "setBackground"));  // NOI18N
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Hintergrund:", Bundle.getMessage(Locale.GERMANY, "setBackground", new Object[]{}));  // NOI18N
        // Using escape for u-with
        Assert.assertEquals("ID-Nummer 1", Bundle.getMessage(Locale.GERMANY, "IDnumber", 1));  // NOI18N
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