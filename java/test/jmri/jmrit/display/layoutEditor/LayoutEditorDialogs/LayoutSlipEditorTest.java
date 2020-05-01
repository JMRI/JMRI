package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.*;

/**
 * Test simple functioning of LayoutSlipEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutSlipEditorTest extends LayoutTurnoutEditorTest {

    @Test
    public void testCtor() {
        new LayoutSlipEditor(null);
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipEditorTest.class);
}
