package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTrackDrawingOptions;
import org.junit.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutTrackDrawingOptionsDialogTest {

    private LayoutEditor le;
    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutTrackDrawingOptions ltdo = new LayoutTrackDrawingOptions("test");
        LayoutTrackDrawingOptionsDialog t = new LayoutTrackDrawingOptionsDialog(le,false,ltdo);
        Assert.assertNotNull("exists",t);
        le.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()) {
            le = new LayoutEditor("Layout Track Drawing Options Dialog Test Layout");
            le.setVisible(true);
        }
    }

    @After
    public void tearDown() {
        if(le!=null){
            EditorFrameOperator efo = new EditorFrameOperator(le);
            efo.closeFrameWithConfirmations();
        }
        jmri.util.JUnitUtil.tearDown();
    }

}
