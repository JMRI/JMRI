package jmri.jmrit.sample.swing;

import org.junit.*;

/**
 * Tests for the ampleConfigStartUpActionFactory class
 *
 * @author	Bob Jacobsen
 */
public class SampleConfigStartUpActionFactoryTest {

    @Test
    // test creation
    public void testCreate() {
        new SampleConfigStartUpActionFactory();
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
