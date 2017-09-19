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
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import jmri.util.JUnitUtil;
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

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testLaunch() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
                
        try {
            // create a custom profile
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/LocoNet_Simulator"), folder.newFolder());
            System.setProperty("org.jmri.profile", "temp/LocoNet_Simulator");

            // launch!
            PanelPro.main(new String[]{"PanelPro"});
        
            // last few messages from a normal startup are:
                // INFO  - Starting with profile LocoNet_Simulator.3eac0cdc [main] apps.Apps.?()
                // INFO  - Using jmri-92FD61C1C87D-3eac0cdc as the JMRI Node identity [main] jmri.util.node.NodeIdentity.?()
                // INFO  - No local configuration found. [main] jmri.jmrix.ConnectionConfigManager.?()
                // INFO  - LocoNet Simulator Started [main] jmrix.loconet.hexfile.LnHexFilePort.?()
                // INFO  - Table preferences not found.
                // This is expected on the first time the "LocoNet Simulator" profile is used on this computer. [main] jmri.swing.JmriJTablePersistenceManager.?()
                // INFO  - File path program: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path preference: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/temp/LocoNet_Simulator/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path profile: is temp/LocoNet_Simulator/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path settings: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/temp/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path home: is /Users/jake/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path scripts: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/jython/ [main] jmri.util.FileUtilSupport.?()
                // WARN  - Cleaning up frame "LocoNet Simulator" (a class jmri.jmrix.loconet.hexfile.HexFileFrame) from earlier test. [main] jmri.util.JUnitUtil.?()
                // WARN  - Cleaning up frame "PanelPro" (a class jmri.util.JmriJFrame) from earlier test. [main] jmri.util.JUnitUtil.?()

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("PanelPro") != null;},"window up");
        
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("PanelPro version") != null;}, "first Info line seen");

            //JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("File path scripts:") != null;}, "last Info line seen");
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("Main initialization done") != null;}, "last Info line seen");

            // maybe have it run a script to indicate that it's really up?
            
            // now clean up frames, depending on what's actually left
                // PanelPro
        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, 5000);
        }
    }


    @Test
    public void testLaunchInitLoop() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
                
        try {
            // create a custom profile
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/Prevent_Init_Loop"), folder.newFolder());
            System.setProperty("org.jmri.profile", "temp/Prevent_Init_Loop");

            // launch!
            PanelPro.main(new String[]{"PanelPro"});
        
            // last few messages from a normal startup are:
                // INFO  - Starting with profile LocoNet_Simulator.3eac0cdc [main] apps.Apps.?()
                // INFO  - Using jmri-92FD61C1C87D-3eac0cdc as the JMRI Node identity [main] jmri.util.node.NodeIdentity.?()
                // INFO  - No local configuration found. [main] jmri.jmrix.ConnectionConfigManager.?()
                // INFO  - LocoNet Simulator Started [main] jmrix.loconet.hexfile.LnHexFilePort.?()
                // INFO  - Table preferences not found.
                // This is expected on the first time the "LocoNet Simulator" profile is used on this computer. [main] jmri.swing.JmriJTablePersistenceManager.?()
                // INFO  - File path program: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path preference: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/temp/LocoNet_Simulator/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path profile: is temp/LocoNet_Simulator/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path settings: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/temp/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path home: is /Users/jake/ [main] jmri.util.FileUtilSupport.?()
                // INFO  - File path scripts: is /Users/jake/Documents/Trains/JMRI/projects/JMRI/jython/ [main] jmri.util.FileUtilSupport.?()
                // WARN  - Cleaning up frame "LocoNet Simulator" (a class jmri.jmrix.loconet.hexfile.HexFileFrame) from earlier test. [main] jmri.util.JUnitUtil.?()
                // WARN  - Cleaning up frame "PanelPro" (a class jmri.util.JmriJFrame) from earlier test. [main] jmri.util.JUnitUtil.?()

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("PanelPro") != null;},"window up");
        
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("PanelPro version") != null;}, "first Info line seen");

            //JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("File path scripts:") != null;}, "last Info line seen");
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("Main initialization done") != null;}, "last Info line seen");

            // maybe have it run a script to indicate that it's really up?
            
            // now clean up frames, depending on what's actually left
                // PanelPro
        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, 5000);
        }
    }
     
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
