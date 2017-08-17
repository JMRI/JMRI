package jmri.jmrit.roster.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

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
        Assert.assertNotNull("exists", frame );
    }

    @Test
    public void testIdentifyEnabled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RosterFrame frame = new RosterFrame();
        frame.setVisible(true);
        RosterFrameScaffold operator = new RosterFrameScaffold(frame.getTitle());
        Assert.assertTrue("Identify Button Enabled", operator.isIdentifyButtonEnabled() );
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initGuiLafPreferencesManager();
        jmri.InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class,new jmri.jmrix.ConnectionConfigManager());
        jmri.InstanceManager.setDefault(jmri.jmrit.symbolicprog.ProgrammerConfigManager.class,new jmri.jmrit.symbolicprog.ProgrammerConfigManager());
        JUnitUtil.initDebugProgrammerManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }


}
