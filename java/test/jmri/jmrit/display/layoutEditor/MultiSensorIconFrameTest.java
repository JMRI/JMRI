package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MultiSensorIconFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class MultiSensorIconFrameTest extends jmri.util.JmriJFrameTestBase {

    private LayoutEditor e;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        e = new LayoutEditor();
        frame = new MultiSensorIconFrame(e);

    }

    @AfterEach
    @Override
    public void tearDown() {
        if(e!=null){
           JUnitUtil.dispose(e);
        }
        e = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }

}
