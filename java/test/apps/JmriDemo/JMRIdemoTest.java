package apps.JmriDemo;

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
import org.junit.rules.Timeout;
import jmri.util.junit.rules.RetryRule;

import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is more of an acceptance test than a unit test. It confirms that the entire
 * application can start up and configure itself.
 * 
 * @author Paul Bender Copyright (C) 2017
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class JMRIdemoTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public Timeout globalTimeout = Timeout.seconds(90); // 90 second timeout for methods in this test class.


    @Test
    @Ignore("ignore test for for app we no longer ship")
    public void testLaunchLocoNet() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
                
        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/LocoNet_Simulator"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            JMRIdemo.main(new String[]{"JMRIdemo"});
            log.debug("started LocoNetSim");

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("JmriDemo") != null;},"window up");
        
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("JmriDemo version") != null;}, "first Info line seen");

            // maybe have it run a script to indicate that it's really up?
            
            // now clean up frames, depending on what's actually left
                // JMRIdemo
        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, 5000);
        }
    }

    @Test
    @Ignore("ignore test for for app we no longer ship")
    public void testLaunchEasyDcc() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/EasyDcc_Simulator"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            JMRIdemo.main(new String[]{"JMRIdemo"});
            log.debug("started EasyDccSim");

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("JmriDemo") != null;},"window up");

            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("JmriDemo version") != null;}, "first Info line seen");


            // JMRIdemo
        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, 5000);
        }
    }

    @Test
    @Ignore("ignore test for for app we no longer ship")
    public void testLaunchTmcc() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/TMCC_Simulator"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            JMRIdemo.main(new String[]{"JMRIdemo"});
            log.debug("started TmcccSim");
            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("JmriDemo") != null;},"window up");

            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("JmriDemo version") != null;}, "first Info line seen");


            // JMRIdemo
        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, 5000);
        }
    }

    @Test
    @Ignore("ignore test for for app we no longer ship")
    public void testLaunchInitLoop() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
                
        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/Prevent_Init_Loop"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            JMRIdemo.main(new String[]{"JMRIdemo"});

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("JmriDemo") != null;},"window up");
        
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("JmriDemo version") != null;}, "first Info line seen");


            // maybe have it run a script to indicate that it's really up?
            
            // now clean up frames, depending on what's actually left
                // JMRIdemo
        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, 5000);
        }
    }
     
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetApplication();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(JMRIdemoTest.class);

}
