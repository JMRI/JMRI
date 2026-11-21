package jmri.jmrix.loconet.sdfeditor;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.loconet.sdf.SdfBuffer;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditorFrameTest extends jmri.util.JmriJFrameTestBase {

    private SdfBuffer b;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        try {
           b = new SdfBuffer("java/test/jmri/jmrix/loconet/sdf/test2.sdf");
        } catch(java.io.IOException ioe){
           Assert.fail("Failed to initialize SdfBuffer");
        }
        if(!GraphicsEnvironment.isHeadless()){
           // a Swing window is made on the GUI thread: made here, its pack() could lay it out while
           // the GUI thread does too, failing at random in GridBagLayout
           SdfBuffer buffer = b;
           frame = jmri.util.ThreadingUtil.runOnGUIwithReturn(() -> new EditorFrame(buffer));
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        b = null;
        JUnitUtil.clearShutDownManager();
        super.tearDown();
    }

}
