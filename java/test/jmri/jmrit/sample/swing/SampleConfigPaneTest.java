package jmri.jmrit.sample.swing;

import org.junit.jupiter.api.*;

/**
 * Tests for the SampleConfigPane class
 *
 * @author Bob Jacobsen
 */
public class SampleConfigPaneTest {

    @Test
    // test creation
    public void testCreate() {
        Assertions.assertNotNull( new SampleConfigPane() );
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
