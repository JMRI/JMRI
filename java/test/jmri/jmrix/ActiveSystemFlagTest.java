package jmri.jmrix;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the XmlFile class.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
public class ActiveSystemFlagTest extends TestCase {

    public void testInactive() throws Exception {
        Assert.assertTrue(!ActiveSystemFlag.isActive("jmri.jmrix.direct"));
    }

    public void testActive() throws Exception {
        jmri.jmrix.grapevine.ActiveFlag.setActive();
        Assert.assertTrue(ActiveSystemFlag.isActive("jmri.jmrix.grapevine"));
    }

    public void testNoSystem() throws Exception {
        try {
            Assert.assertTrue(ActiveSystemFlag.isActive("jmri.foo"));
            Assert.fail("Didn't throw exception");
        } catch (Exception e) {
        }
    }

    // from here down is testing infrastructure
    public ActiveSystemFlagTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {ActiveSystemFlagTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ActiveSystemFlagTest.class);
        return suite;
    }

    // protected access for subclass
    // private final static Logger log = LoggerFactory.getLogger(ActiveSystemFlagTest.class);

}
