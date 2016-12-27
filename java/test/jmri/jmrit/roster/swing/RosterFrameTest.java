package jmri.jmrit.roster.swing;

import apps.tests.Log4JFixture;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
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
        RosterFrame frame = new RosterFrame(){
            // for now, don't show the status bar in the test, so
            // we can sort out how to initialize the ProfileManager
            // for tests.
            @Override
            protected void statusBar(){
            };

            // remoteCalls shouldn't need to be overridden, but if
            // we don't do this, we get a large number of runtime error
            // messages stating it doesn't exist.
            @Override
            public void remoteCalls(String[] args){
                super.remoteCalls(args);
            }
            
        };
        Assert.assertNotNull("exists", frame );
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
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
