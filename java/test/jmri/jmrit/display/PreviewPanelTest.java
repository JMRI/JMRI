package jmri.jmrit.display;

import org.junit.Assert;
import org.junit.Test;

import jmri.jmrit.display.EditorScaffold;
import jmri.util.JUnitUtil;

/**
 * Tests for the PreviewPanel class
 *
 * @author Pete Cressman Copyright (C) 2020
 */
public class PreviewPanelTest  {

    @Test
    public void testCtor() {
        EditorScaffold editor = new EditorScaffold("ED");
        DisplayFrame df = new DisplayFrame("DisplayFrame", editor);
        PreviewPanel p = new PreviewPanel(df, null, null, true);
        Assert.assertNotNull("PreviewPanel Constructor",p);
        JUnitUtil.dispose(df);
        JUnitUtil.dispose(editor);
    }
    
}
