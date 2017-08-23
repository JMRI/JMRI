package apps.PanelPro;

import java.awt.GraphicsEnvironment;
import java.io.File;

import org.apache.commons.io.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PanelProTest {

    @Test
    // @Ignore("Causes Exception")
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PanelPro t = new PanelPro();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testLaunch() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // create a custom profile
        
        //FileUtils.copyDirectory(new File("foo"), new File("bar"));
        System.setProperty("org.jmri.profile", "java/test/apps/PanelPro/profiles/LocoNet_Simulator");
        System.out.println("confirm as "+System.getProperty("org.jmri.profile"));
        PanelPro.main(new String[]{"PanelPro"});  // <-- can we point to a pre-made profile here somehow?
        
        // last few messages from a normal startup are:
            // INFO  - No saved user preferences file [main]
            // INFO  - Did not find throttle preferences file.  This is normal if you haven't save the preferences before [init prefs]
            // INFO  - Could not find WiThrottle preferences file (/Users/jake/Library/Preferences/JMRI/Nwe/throttle/WiThrottlePreferences.xml).  Normal if preferences have not been saved before. [init

        JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("Could not find WiThrottle preferences") != null;},"init complete INFO message");
        
        // now clean up frames, depending on what's actually left
        
    }

     
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager(); // an app should recreate this, but just in case
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
