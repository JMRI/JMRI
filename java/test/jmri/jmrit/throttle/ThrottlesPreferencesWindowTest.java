package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

import jmri.util.junit.rules.RetryRule;

/**
 * Test simple functioning of ThrottlesPreferencesWindow
 *
 * @author Lionel Jeanson
 */
public class ThrottlesPreferencesWindowTest {

    public RetryRule retryRule = new RetryRule(2);  // allow 2 retries
                                                    // because of possible IndexOutOfBoundsException
                                                    // in Swing during tearDown
    @Test
    public void testCtor() {
        try {
            Assume.assumeFalse(GraphicsEnvironment.isHeadless());
            ThrottlesPreferencesWindow w = new ThrottlesPreferencesWindow("ThrottlesPreferencesWindowTest");
            Assert.assertNotNull("exists", w);
        } catch (IndexOutOfBoundsException e) {
            Assert.fail("IndexOutOfBoundsException, fail to retry\n"+e);
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();

    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }
}

