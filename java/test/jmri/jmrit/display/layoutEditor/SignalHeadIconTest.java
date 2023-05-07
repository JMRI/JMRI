package jmri.jmrit.display.layoutEditor;

import jmri.jmrit.display.SignalHeadIcon;
import jmri.util.JUnitUtil;

import org.junit.Assert;

import org.junit.jupiter.api.*;

import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of SignalHeadIcon.
 *
 * @author Paul Bender Copyright (C) 2016
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",
    justification = "see comment in tested class, this file needs to use the tested class name.")
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class SignalHeadIconTest extends jmri.jmrit.display.SignalHeadIconTest {

    protected SignalHeadIcon shi = null;

    @Test
    @Override
    public void testCtor() {
        Assert.assertNotNull("SignalHeadIcon Constructor", p);
    }

    @Test
    @Override
    public void testGetAppearance() {
        Assert.assertEquals("SignalHeadIcon Name", "IH1", shi.getNameString());
        Assert.assertEquals("SignalHeadIcon Initial Appearance", 0, shi.headState());
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSignalHeadManager();

        editor = new LayoutEditor();
        shi = new SignalHeadIcon(editor);
        jmri.implementation.VirtualSignalHead h = new jmri.implementation.VirtualSignalHead("IH1");
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        shi.setSignalHead(new jmri.NamedBeanHandle<>("IH1", h));
        p = shi;

    }

    @AfterEach
    @Override
    public void tearDown() {
        if (shi != null) {
           shi.getEditor().dispose();
           p = null;
        }
        super.tearDown();
    }

}
