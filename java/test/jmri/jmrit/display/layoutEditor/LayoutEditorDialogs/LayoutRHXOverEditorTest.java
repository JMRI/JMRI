package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.*;

/**
 * Test simple functioning of LayoutRHXOverEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutRHXOverEditorTest extends LayoutXOverEditorTest {

    @Test
    public void testCtor() {
        new LayoutRHXOverEditor(null);
    }
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown()  {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHOXOverEditorTest.class);
}
