package apps;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the ConfigBundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class ConfigBundleTest extends TestCase {

    public void testGoodKeysMessage() {
        Assert.assertEquals("File", ConfigBundle.getMessage("MenuFile"));
        Assert.assertEquals("Turnout", ConfigBundle.getMessage("BeanNameTurnout"));
    }

    public void testBadKeyMessage() {
        try {
            ConfigBundle.getMessage("FFFFFTTTTTTT");
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    public void testGoodKeysMessageArg() {
        Assert.assertEquals("File", ConfigBundle.getMessage("MenuFile", new Object[]{}));
        Assert.assertEquals("Turnout", ConfigBundle.getMessage("BeanNameTurnout", new Object[]{}));
    }

    public void testBadKeyMessageArg() {
        try {
            ConfigBundle.getMessage("FFFFFTTTTTTT", new Object[]{});
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    // from here down is testing infrastructure
    public ConfigBundleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ConfigBundleTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ConfigBundleTest.class);
        return suite;
    }

}
