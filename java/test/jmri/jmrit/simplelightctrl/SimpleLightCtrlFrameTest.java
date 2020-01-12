package jmri.jmrit.simplelightctrl;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of SimpleLightCtrlFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SimpleLightCtrlFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new SimpleLightCtrlFrame();
	}
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
