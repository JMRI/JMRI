package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutRHXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutRHXOverEditorTest extends LayoutXOverEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutRHXOverEditor(null);
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHOXOverEditorTest.class);
}
