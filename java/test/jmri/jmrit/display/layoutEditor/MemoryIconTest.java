package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;

/**
 * Test simple functioning of MemoryIcon.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MemoryIconTest extends jmri.jmrit.display.MemoryIconTest {

    @Test
    @Override
    @Ignore("Superclass method assumes graphical icon (red X)")
    @ToDo("rewrite superclass test so it works in this case.")
    public void testShowEmpty() {
    }

    @Test
    @Override
    @Ignore("When test from superclass is run, Scale is not set")
    @ToDo("rewrite superclass test so it works in this case.")
    public void testGetAndSetScale(){
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new LayoutEditor();
            p = to = new MemoryIcon("MemoryTest1", (LayoutEditor)editor );
            to.setMemory("IM1");
        }
    }

    @After
    @Override
    public void tearDown() {
        if (to != null) {
           to.getEditor().dispose();
           to = null;
           p = null;
        }
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

}
