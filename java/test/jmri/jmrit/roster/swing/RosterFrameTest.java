package jmri.jmrit.roster.swing;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of RosterFrame
 *
 * @author	Paul Bender Copyright (C) 2015,2016
 */
public class RosterFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterFrame frame = new RosterFrame();
        Assert.assertNotNull("exists", frame);
        JUnitUtil.dispose(frame);
    }

    @Test
    public void testIdentifyEnabled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterFrame frame = new RosterFrame();
        frame.setVisible(true);
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        Assert.assertTrue("Identify Button Enabled", operator.isIdentifyButtonEnabled());
        JUnitUtil.dispose(frame);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initGuiLafPreferencesManager();
        jmri.InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class, new jmri.jmrix.ConnectionConfigManager());
        jmri.InstanceManager.setDefault(jmri.jmrit.symbolicprog.ProgrammerConfigManager.class, new jmri.jmrit.symbolicprog.ProgrammerConfigManager());
        JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }

}
