package jmri.jmrit.withrottle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of ControllerFilterFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ControllerFilterFrameTest extends jmri.util.JmriJFrameTestBase {

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new ControllerFilterFrame();
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }
}
