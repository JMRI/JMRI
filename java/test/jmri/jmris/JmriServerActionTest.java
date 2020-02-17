package jmri.jmris;

import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the jmri.jmris.JmriServerAction class
 *
 * @author Paul Bender
 */
public class JmriServerActionTest {

    @Test
    public void testCtorDefault() {
        JmriServerAction a = new JmriServerAction();
        Assert.assertNotNull(a);
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }

}
