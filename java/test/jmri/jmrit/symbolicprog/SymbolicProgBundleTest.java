package jmri.jmrit.symbolicprog;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the SymbolicProgBundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class SymbolicProgBundleTest extends TestCase {

    public void testGoodKeys() {
        Assert.assertEquals("Read", Bundle.getMessage("ButtonRead"));
        Assert.assertEquals("Tools", Bundle.getMessage("MenuTools"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    public void testBadKey() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT");
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    // from here down is testing infrastructure
    public SymbolicProgBundleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SymbolicProgBundleTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SymbolicProgBundleTest.class);
        return suite;
    }

}
