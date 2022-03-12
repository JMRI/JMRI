package jmri.jmris;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.JmriServer class 
 *
 * @author Paul Bender
 */
public class JmriServerTest {

    @Test
    public void testCtorDefault() {
        JmriServer a = new JmriServer();
        assertThat(a).isNotNull();
    }

    @Test
    public void testCtorPort() {
        JmriServer a = new JmriServer(25520);
        assertThat(a).isNotNull();
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 25520");
    }

    @Test
    public void testCtorPortAndTimeout() {
        JmriServer a = new JmriServer(25520,100);
        assertThat(a).isNotNull();
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 25520");
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
