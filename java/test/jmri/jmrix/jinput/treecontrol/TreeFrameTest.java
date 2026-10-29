package jmri.jmrix.jinput.treecontrol;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of TreeFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfHeadless
public class TreeFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        frame = new TreeFrame();
    }

    @AfterEach
    @Override
    public void tearDown() {
        // GitHub CI workflows doesn't have a working HID system
        JUnitAppender.suppressWarnMessage("No controllers found; tool is probably not working");
        JUnitAppender.suppressWarnMessage("loading of HID System failed");

        JUnitUtil.tearDown();
    }

}
