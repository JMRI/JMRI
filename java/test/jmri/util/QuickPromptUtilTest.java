package jmri.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.function.Predicate;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class QuickPromptUtilTest {

    @Test
    public void testCTor() {
        QuickPromptUtil t = new QuickPromptUtil();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
    
    /**
     * Checks that int predicate works well.
     */
    @Test
    public void testIntRangePredicate() {
        doTestIntRangePredicate("someValue");
    }
    
    @Test
    public void testIntRangePredicateWithLabel() {
        doTestIntRangePredicate("someValue");
    }
    
    private void doTestIntRangePredicate(String label) {
        Predicate<Integer> pr = new QuickPromptUtil.IntRangePredicate(
                null, 10, label);
        assertTrue(pr.test(-1));
        assertTrue(pr.test(5));
        try {
            assertTrue(pr.test(15));
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            if (label != null) {
                assertTrue(ex.getLocalizedMessage().contains(label));
            }
            assertTrue(ex.getLocalizedMessage().contains("10"));
            assertFalse(ex.getLocalizedMessage().contains("null"));
        }
        
        pr = new QuickPromptUtil.IntRangePredicate(
                10, null, label);
        assertTrue(pr.test(15));

        try {
            assertTrue(pr.test(5));
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            if (label != null) {
                assertTrue(ex.getLocalizedMessage().contains(label));
            }
            assertTrue(ex.getLocalizedMessage().contains("10"));
            assertFalse(ex.getLocalizedMessage().contains("null"));
        }
        
        pr = new QuickPromptUtil.IntRangePredicate(
                10, 20, label);
        assertTrue(pr.test(15));

        try {
            assertTrue(pr.test(5));
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            if (label != null) {
                assertTrue(ex.getLocalizedMessage().contains(label));
            }
            assertTrue(ex.getLocalizedMessage().contains("10"));
            assertFalse(ex.getLocalizedMessage().contains("null"));
        }
        try {
            assertTrue(pr.test(25));
            fail("Exception expected");
        } catch (IllegalArgumentException ex) {
            if (label != null) {
                assertTrue(ex.getLocalizedMessage().contains(label));
            }
            assertTrue(ex.getLocalizedMessage().contains("20"));
            assertFalse(ex.getLocalizedMessage().contains("null"));
        }
    }
}
