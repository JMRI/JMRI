package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of SignalHeadIcon.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SignalHeadIconTest extends PositionableIconTest {

    protected SignalHeadIcon shi = null;

    @Test
    @Override
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("SignalHeadIcon Constructor", p);
    }

    @Test
    public void testGetAppearance() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("SignalHeadIcon Name", "IH1", p.getNameString());
        Assert.assertEquals("SignalHeadIcon Initial Appearance", 0, shi.headState());
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            shi = new SignalHeadIcon(editor);
            jmri.implementation.VirtualSignalHead h = new jmri.implementation.VirtualSignalHead("IH1");
            jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
            shi.setSignalHead(new jmri.NamedBeanHandle<>("IH1", h));
            p = shi;
        }
    }

}
