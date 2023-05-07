package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MemoryIcon.
 *
 * @author Paul Bender Copyright (C) 2016
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
    justification = "see comment in tested class, this file needs to use the tested class name.")
public class MemoryIconTest extends jmri.jmrit.display.MemoryIconTest {

    @Test
    @Override
    @Disabled("Superclass method assumes graphical icon (red X)")
    @ToDo("rewrite superclass test so it works in this case.")
    public void testShowEmpty() {
    }

    @Test
    @Override
    @Disabled("When test from superclass is run, Scale is not set")
    @ToDo("rewrite superclass test so it works in this case.")
    public void testGetAndSetScale(){
    }

    @BeforeEach
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

    @AfterEach
    @Override
    public void tearDown() {
        if (to != null) {
           to.getEditor().dispose();
           to = null;
           p = null;
        }
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
