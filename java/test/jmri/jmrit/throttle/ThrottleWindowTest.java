package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of ThrottleWindow
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ThrottleWindowTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        if (!GraphicsEnvironment.isHeadless()) {
            frame = new ThrottleWindow();
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
