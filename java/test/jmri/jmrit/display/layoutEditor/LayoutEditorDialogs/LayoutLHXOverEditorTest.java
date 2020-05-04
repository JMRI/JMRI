package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.*;

/**
 * Test simple functioning of LayoutLHXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutLHXOverEditorTest extends LayoutXOverEditorTest {

    @Test
    public void testCtor() {
        new LayoutLHXOverEditor(null);

        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutLHXOverEditorTest.class);
}
