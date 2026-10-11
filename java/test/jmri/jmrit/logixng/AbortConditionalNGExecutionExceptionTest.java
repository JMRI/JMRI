package jmri.jmrit.logixng;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test AbortConditionalNGExecutionException
 *
 * @author Daniel Bergqvist 2021
 */
public class AbortConditionalNGExecutionExceptionTest {

    @Test
    public void testCtor() {
        AbortConditionalNGExecutionException t = new AbortConditionalNGExecutionException(null, null);
        Assertions.assertNotNull( t, "not null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
