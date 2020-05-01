package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.*;

/**
 * Test simple functioning of LayoutTurnoutEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTurnoutEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        new LayoutTurnoutEditor(null);
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown()  {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutEditorTest.class);
}
