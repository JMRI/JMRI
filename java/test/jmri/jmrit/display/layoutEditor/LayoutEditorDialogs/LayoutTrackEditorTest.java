package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.*;

/**
 * Test simple functioning of LayoutTrackEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        new LayoutTrackEditor(null);
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown()  {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LayoutTrackEditorTest.class);
}
