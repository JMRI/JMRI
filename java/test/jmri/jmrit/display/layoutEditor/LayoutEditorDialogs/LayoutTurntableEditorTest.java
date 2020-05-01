package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.*;

/**
 * Test simple functioning of LayoutTurntableEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTurntableEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        new LayoutTurntableEditor(null);
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown()  {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurntableEditorTest.class);
}
