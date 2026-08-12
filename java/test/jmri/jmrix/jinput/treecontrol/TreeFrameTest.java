package jmri.jmrix.jinput.treecontrol;

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

}
