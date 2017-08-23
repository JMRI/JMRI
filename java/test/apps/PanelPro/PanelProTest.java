package apps.PanelPro;

import java.awt.GraphicsEnvironment;
import java.io.*;

import org.apache.commons.io.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

/**
 * This is more of an acceptance test than a unit test. It confirms that the entire
 * application can start up and configure itself.
 * 
 * @author Paul Bender Copyright (C) 2017
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class PanelProTest {

    //@Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PanelPro t = new PanelPro();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testLaunch() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
                
        try {
            // create a custom profile
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/LocoNet_Simulator"), new File("temp/"));
            System.setProperty("org.jmri.profile", "temp/LocoNet_Simulator");

            // launch!
            PanelPro.main(new String[]{"PanelPro"});
        
            // last few messages from a normal startup are:
                // INFO  - ****** JMRI log ******* [main] jmri.util.Log4JUtil.?()
                // INFO  - PanelPro version 4.9.4ish+jake+20170823T1324Z+R0746e45604 starts under Java 1.8.0_144 on Mac OS X x86_64 v10.12.6 at Wed Aug 23 06:24:08 PDT 2017 [main] apps.Apps.?()
                // WARN  - Unable to set active profile. No profile with id temp/LocoNet_Simulator could be found. [main] jmri.profile.ProfileManager.?()
                // INFO  - Starting with profile My_JMRI_Railroad.3f72daeb [main] apps.Apps.?()
                // INFO  - No saved preferences, will open preferences window.  Searched for /Users/jake/Documents/Trains/JMRI/projects/JMRI/temp/My_JMRI_Railroad/ProfileConfig.xml [main] apps.Apps.?()
                // INFO  - File path program: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path preference: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/temp/My_JMRI_Railroad/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path profile: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/temp/My_JMRI_Railroad/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path settings: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/temp/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path home: is /Users/jake/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path scripts: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/jython/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - Using jmri-92FD61C1C87D-3f72daeb as the JMRI Node identity [main] jmri.util.node.NodeIdentity.?()

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("PanelPro") != null;},"window up");
        
            // maybe have it run a script to indicate that it's really up?
            
            // now clean up frames, depending on what's actually left
                // PanelPro
        } finally {
        
            // need to clean up the temp directory
        }
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
