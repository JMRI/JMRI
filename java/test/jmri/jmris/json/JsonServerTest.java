package jmri.jmris.json;

import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmris.json package
 *
 * @author Paul Bender
 */
public class JsonServerTest {

    @Test
    public void testCtor() {
        JsonServer a = new JsonServer();
        assertThat(a).isNotNull();
    }

    @Test
    public void testCtorwithParameter() {
        JsonServer a = new JsonServer(12345, 10000);
        //jmri.util.JUnitAppender.assertErrorMessage("Failed to connect to port 12345");
        assertThat(a).isNotNull();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
