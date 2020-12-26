package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.PositionableIconTest;
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
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", p);
    }

    @Override
    @Test
//    @Disabled("not supported for PortalIcon")
    public void testDoViemMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Do View Menu", p.doViemMenu());
    }

    @Override
    @Test
    @Disabled("a PortalIcon constructed with just an Editor does not have an associated portal, so this fails")
    public void testGetNameString() {
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
//        jmri.util.JUnitAppender.assertWarnMessage("getIconMap failed. family \"null\" not found in item type \"Portal\"");
        if (editor != null) {
            JUnitUtil.dispose(editor);
        }
        super.tearDown();
    }

}
