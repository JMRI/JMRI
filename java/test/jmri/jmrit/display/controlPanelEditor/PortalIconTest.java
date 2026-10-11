package jmri.jmrit.display.controlPanelEditor;

import jmri.jmrit.display.PositionableIconTest;
import jmri.jmrit.logix.Portal;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of the PortalIcon class.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PortalIconTest extends PositionableIconTest {

    @Test
    @Override
    @DisabledIfHeadless
    public void testCtor() {
        Assertions.assertNotNull(p, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testPortalCtor() {
        Portal po1 = new Portal("Po1");
        PortalIcon p1 = new PortalIcon(editor, po1);
        Assertions.assertNotNull(p1, "exists");
    }

    @Override
    @Test
    @DisabledIfHeadless
    public void testDoViemMenu() {
        Assertions.assertFalse(p.doViemMenu(), "Do View Menu");
    }

    @Override
    @Test
    @DisabledIfHeadless
    public void testGetNameString() {
        Portal po2 = new Portal("Name String");
        PortalIcon p2 = new PortalIcon(editor, po2);
        Assertions.assertNotNull(p2.getNameString(), "Name String");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // creates EditorScaffold
        JUnitUtil.dispose(editor);
        editor = new ControlPanelEditor("Portal Icon Test Panel");
        p = new PortalIcon(editor);
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
