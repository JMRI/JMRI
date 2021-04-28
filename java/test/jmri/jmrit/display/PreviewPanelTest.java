package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.util.JUnitUtil;

/**
 * Tests for the PreviewPanel class
 *
 * @author Pete Cressman Copyright (C) 2020
 */
public class PreviewPanelTest  {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EditorScaffold editor = new EditorScaffold("ED");
        DisplayFrame df = new DisplayFrame("DisplayFrame", editor);
        PreviewPanel p = new PreviewPanel(df, null, null, true);
        Assert.assertNotNull("PreviewPanel Constructor",p);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
        
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.deregisterBlockManagerShutdownTask();
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }
    
    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
