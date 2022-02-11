package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the AssociateFrame class.
 * @author Steve Young Copyright (C) 2022
 */
public class AssociateFrameTest extends jmri.util.JmriJFrameTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        AssociateTag panel = new AssociateTag("unknownTag");
        frame = new AssociateFrame(panel,"Test Associate Frame");
    }

}
