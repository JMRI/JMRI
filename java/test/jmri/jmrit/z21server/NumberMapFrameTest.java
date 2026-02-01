package jmri.jmrit.z21server;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of ControllerFilterFrame
 *
 * @author Eckart Meyer (C) 2025
 */
@DisabledIfHeadless
public class NumberMapFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        frame = new NumberMapFrame();
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
