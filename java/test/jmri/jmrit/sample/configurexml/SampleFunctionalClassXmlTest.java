package jmri.jmrit.sample.configurexml;

import org.junit.jupiter.api.*;

/**
 * Tests for the SampleFunctionalClass class
 *
 * @author Bob Jacobsen
 */
public class SampleFunctionalClassXmlTest {

    @Test
    // test creation
    public void testCreate() {
        new SampleFunctionalClassXml();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
