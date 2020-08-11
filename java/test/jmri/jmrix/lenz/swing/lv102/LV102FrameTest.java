package jmri.jmrix.lenz.swing.lv102;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmrix.lenz.swing.lv102.LV102Frame class
 *
 * @author Paul Bender
 */
public class LV102FrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testCloseButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        assertThat(frame.isVisible()).isTrue();
        LV102FrameScaffold operator = new LV102FrameScaffold();
        operator.pushCloseButton();
        assertThat(frame.isVisible()).isFalse();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new LV102Frame();
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
