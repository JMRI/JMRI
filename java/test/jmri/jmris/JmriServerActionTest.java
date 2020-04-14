package jmri.jmris;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.JmriServerAction class
 *
 * @author Paul Bender
 */
public class JmriServerActionTest {

    @Test
    public void testCtorDefault() {
        JmriServerAction a = new JmriServerAction();
        assertThat(a).isNotNull();
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
