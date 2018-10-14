package jmri;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.*;

/**
 *
 * @author zoo
 */
public class VersionTest {

    /**
     * Test of isCanonicalVersion method, of class Version.
     */
    @Test
    public void testIsCanonicalVersion() {

        assertTrue(Version.isCanonicalVersion("1.2.3"));
        assertFalse(Version.isCanonicalVersion("1.2"));
        assertTrue(Version.isCanonicalVersion("1.2.3-p"));
        assertTrue(Version.isCanonicalVersion("1.2.0"));
        assertFalse(Version.isCanonicalVersion("1.2.0ish"));
    }

    /**
     * Test of isCanonicalVersion method, of class Version.
     */
    @Test
    public void testCompareCanonicalVersions() {

        assertTrue(Version.compareCanonicalVersions("1.2.3", "1.2.3") == 0);
        assertTrue(Version.compareCanonicalVersions("1.2.1", "1.2.3") < 0);
        assertTrue(Version.compareCanonicalVersions("1.2.4", "1.2.3") > 0);
        assertTrue(Version.compareCanonicalVersions("1.2.3-P", "1.2.3-P") == 0);
        assertTrue(Version.compareCanonicalVersions("1.2.4", "1.2.3-P") > 0);
        assertTrue(Version.compareCanonicalVersions("1.2.3-P", "1.2.3") == 0);
        assertTrue(Version.compareCanonicalVersions("213.1.1", "213.1.1") == 0);
        assertTrue(Version.compareCanonicalVersions("213.1.1", "213.1.10") < 0);
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
