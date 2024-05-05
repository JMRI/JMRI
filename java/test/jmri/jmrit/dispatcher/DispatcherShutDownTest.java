package jmri.jmrit.dispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import java.awt.GraphicsEnvironment;
import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DispatcherShutDownTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DispatcherShutDownTask t = new DispatcherShutDownTask("test dispatcher shutdown task");
        assertThat(t).withFailMessage("exists").isNotNull();
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
