/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author zoo
 */
public class VersionTest extends TestCase {

    public VersionTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of isCanonicalVersion method, of class Version.
     */
    public void testIsCanonicalVersion() {

        assertTrue(Version.isCanonicalVersion("1.2.3"));
        assertFalse(Version.isCanonicalVersion("1.2"));

    }

    /**
     * Test of isCanonicalVersion method, of class Version.
     */
    public void testCompareCanonicalVersions() {

        assertTrue(Version.compareCanonicalVersions("1.2.3", "1.2.3") == 0);
        assertTrue(Version.compareCanonicalVersions("1.2.1", "1.2.3") < 0);
        assertTrue(Version.compareCanonicalVersions("1.2.4", "1.2.3") > 0);

        assertTrue(Version.compareCanonicalVersions("213.1.1", "213.1.1") == 0);
        assertTrue(Version.compareCanonicalVersions("213.1.1", "213.1.10") < 0);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(VersionTest.class);
        return suite;
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", VersionTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
}
