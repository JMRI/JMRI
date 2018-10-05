package jmri.jmrit.sample;

import org.junit.*;

/**
 * Tests for the SampleFunctionalClass class
 *
 * @author	Bob Jacobsen
 */
public class SampleFunctionalClassTest {

    @Test
    // test creation
    public void testCreate() {
        new SampleFunctionalClass("foo");
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
