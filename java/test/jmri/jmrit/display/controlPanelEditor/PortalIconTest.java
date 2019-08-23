package jmri.jmrit.display.controlPanelEditor;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.PositionableIconTest;
import org.junit.*;

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
//    @Ignore("not supported for PortalIcon")
    public void testDoViemMenu() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertFalse("Do View Menu", p.doViemMenu());
    }

    @Override
    @Test
    @Ignore("a PortalIcon constructed with just an Editor does not have an associated portal, so this fails")
    public void testGetNameString() {
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new ControlPanelEditor("Portal Icon Test Panel");
            p = new PortalIcon(editor);
        }
    }

}
