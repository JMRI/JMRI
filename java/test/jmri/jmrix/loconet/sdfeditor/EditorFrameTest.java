package jmri.jmrix.loconet.sdfeditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.loconet.sdf.SdfBuffer;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EditorFrameTest extends jmri.util.JmriJFrameTestBase {
           
    private SdfBuffer b;

    // The minimal setup for log4J
    @Before
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
           frame = new EditorFrame(b);
        }
    }

    @After
    @Override
    public void tearDown() {
        b = null;
        super.tearDown();
    }

}
