package jmri.jmrit.sample.swing;

import org.junit.*;

/**
 * Tests for the SampleConfigPane class
 *
 * @author	Bob Jacobsen
 */
public class SampleConfigPaneTest {

    @Test
    // test creation
    public void testCreate() {
        new SampleConfigPane();
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
