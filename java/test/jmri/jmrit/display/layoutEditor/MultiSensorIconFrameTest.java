package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MultiSensorIconFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MultiSensorIconFrameTest extends jmri.util.JmriJFrameTestBase {

    private LayoutEditor e;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           e = new LayoutEditor();
           frame = new MultiSensorIconFrame(e);
        }

    }

    @AfterEach
    @Override
    public void tearDown() {
        if(e!=null){
           JUnitUtil.dispose(e);
        }
        e = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }

}
