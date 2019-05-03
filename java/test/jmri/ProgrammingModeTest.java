package jmri;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the ProgrammingMode class
 *
 * @author Bob Jacobsen Copyright (C) 2014
 */
public class ProgrammingModeTest {

    @Test
    public void testStateCtors() {
        // tests that statics exist, are not equal
        Assert.assertTrue(ProgrammingMode.PAGEMODE.equals(ProgrammingMode.PAGEMODE));
        Assert.assertTrue(!ProgrammingMode.REGISTERMODE.equals(ProgrammingMode.PAGEMODE));
    }

}
