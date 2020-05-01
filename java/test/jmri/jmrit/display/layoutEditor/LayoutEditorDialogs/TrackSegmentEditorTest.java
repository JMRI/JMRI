package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.*;

/**
 * Test simple functioning of TrackSegmentEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class TrackSegmentEditorTest extends LayoutTrackEditorTest {

    @Test
    public void testCtor() {
        new TrackSegmentEditor(null);
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown()  {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackSegmentEditorTest.class);
}
