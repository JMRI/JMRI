package jmri.jmrit.display.layoutEditor;

import jmri.jmrit.display.SignalHeadIcon;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
import org.junit.*;

import java.awt.*;

/**
 * Test simple functioning of SignalHeadIcon.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SignalHeadIconTest extends jmri.jmrit.display.SignalHeadIconTest {

    protected SignalHeadIcon shi = null;

    @Test
    @Override
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("SignalHeadIcon Constructor", p);
    }

    @Test
    @Override
    public void testGetAppearance() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("SignalHeadIcon Name", "IH1", shi.getNameString());
        Assert.assertEquals("SignalHeadIcon Initial Appearance", 0, shi.headState());
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new LayoutEditor();
            shi = new SignalHeadIcon(editor);
            jmri.implementation.VirtualSignalHead h = new jmri.implementation.VirtualSignalHead("IH1");
            jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
            shi.setSignalHead(new jmri.NamedBeanHandle<>("IH1", h));
            p = shi;
        }
    }

    @After
    @Override
    public void tearDown() {
        if (shi != null) {
           shi.getEditor().dispose();
           p = null;
        }
        super.tearDown();
    }

}
