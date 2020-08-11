package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutDoubleXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutDoubleXOverEditorTest extends LayoutXOverEditorTest {

    @Test
    public void testCtor() {
        new LayoutDoubleXOverEditor(null);

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
    }
 
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutDoubleXOverEditorTest.class);
}
