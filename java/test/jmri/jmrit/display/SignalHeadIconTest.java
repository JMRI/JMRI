package jmri.jmrit.display;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of SignalHeadIcon.
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class SignalHeadIconTest extends PositionableIconTest {

    private SignalHeadIcon sHIcon = null;

    @Test
    @Override
    public void testCtor() {

        Assert.assertNotNull("SignalHeadIcon Constructor", p);
    }

    @Test
    public void testGetAppearance() {

        Assert.assertEquals("SignalHeadIcon Name", "IH1", p.getNameString());
        Assert.assertEquals("SignalHeadIcon Initial Appearance", 0, sHIcon.headState());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSignalHeadManager();

        editor = new EditorScaffold();
        sHIcon = new SignalHeadIcon(editor);
        jmri.implementation.VirtualSignalHead h = new jmri.implementation.VirtualSignalHead("IH1");
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).register(h);
        sHIcon.setSignalHead(new jmri.NamedBeanHandle<>("IH1", h));
        p = sHIcon;

    }

}
