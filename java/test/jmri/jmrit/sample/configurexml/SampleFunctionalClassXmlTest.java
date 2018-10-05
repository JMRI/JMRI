package jmri.jmrit.sample.configurexml;

import org.junit.*;

/**
 * Tests for the SampleFunctionalClass class
 *
 * @author	Bob Jacobsen
 */
public class SampleFunctionalClassXmlTest {

    @Test
    // test creation
    public void testCreate() {
        new SampleFunctionalClassXml();
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
