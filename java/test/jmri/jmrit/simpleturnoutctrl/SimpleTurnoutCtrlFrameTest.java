package jmri.jmrit.simpleturnoutctrl;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of SimpleTurnoutCtrlFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SimpleTurnoutCtrlFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SimpleTurnoutCtrlFrame();
	}
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
