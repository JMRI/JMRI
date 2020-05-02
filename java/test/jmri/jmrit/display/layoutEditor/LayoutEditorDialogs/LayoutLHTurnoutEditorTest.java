package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.*;

/**
 * Test simple functioning of LayoutLHTurnoutEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutLHTurnoutEditorTest extends LayoutTurnoutEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutLHTurnoutEditor(null);
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutLHTurnoutEditorTest.class);
}
