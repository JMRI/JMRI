package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.PositionableIconTest;
import jmri.jmrit.logix.Portal;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of the PortalIcon class.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PortalIconTest extends PositionableIconTest {

    @Test
    @Override
    public void testCtor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assertions.assertNotNull(p, "exists");
    }

    @Test
    public void testPortalCtor() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Portal po1 = new Portal("Po1");
        PortalIcon p1 = new PortalIcon(editor, po1);
        Assertions.assertNotNull(p1, "exists");
    }

    @Override
    @Test
//    @Disabled("not supported for PortalIcon")
    public void testDoViemMenu() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Assertions.assertFalse(p.doViemMenu(), "Do View Menu");
    }

    @Override
    @Test
    public void testGetNameString() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        Portal po2 = new Portal("Name String");
        PortalIcon p2 = new PortalIcon(editor, po2);
        Assertions.assertNotNull(p2.getNameString(), "Name String");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new ControlPanelEditor("Portal Icon Test Panel");
            p = new PortalIcon(editor);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (editor != null) {
            JUnitUtil.dispose(editor);
        }
        super.tearDown();
    }

}
