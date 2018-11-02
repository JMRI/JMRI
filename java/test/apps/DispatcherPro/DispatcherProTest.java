package apps.DispatcherPro;

import java.awt.GraphicsEnvironment;
import java.io.*;

import org.apache.commons.io.*;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import jmri.InstanceManager;
import jmri.managers.DefaultShutDownManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.junit.rules.RetryRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is more of an acceptance test than a unit test. It confirms that the entire
 * application can start up and configure itself.
 * 
 * @author Paul Bender Copyright (C) 2017
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class DispatcherProTest {

    static final int RELEASETIME = 3000;  // mSec
    static final int TESTMAXTIME = 20;    // seconds - not too long, so job doesn't hang

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public Timeout globalTimeout = Timeout.seconds(TESTMAXTIME);

    @Rule
    public RetryRule retryRule = new RetryRule(1); // allow 1 retry

    @Test
    public void testLaunchLocoNet() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/LocoNet_Simulator"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            DispatcherPro.main(new String[]{"DispatcherPro"});
            log.debug("started LocoNetSim");

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("DispatcherPro") != null;}, "window up");
        
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("DispatcherPro version") != null;}, "first Info line seen");

            // maybe have it run a script to indicate that it's really up?
            
            // now clean up frames, depending on what's actually left
            // DispatcherPro

            // gracefully shutdown, but don't exit
            ((DefaultShutDownManager)InstanceManager.getDefault(jmri.ShutDownManager.class)).shutdown(0, false);

        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, RELEASETIME);
        }
    }

    @Test
    public void testLaunchEasyDcc() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/EasyDcc_Simulator"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            DispatcherPro.main(new String[]{"DispatcherPro"});
            log.debug("started EasyDccSim");

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("DispatcherPro") != null;}, "window up");

            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("DispatcherPro version") != null;}, "first Info line seen");

            // now clean up frames, depending on what's actually left
            // DispatcherPro

            // gracefully shutdown, but don't exit
            ((DefaultShutDownManager)InstanceManager.getDefault(jmri.ShutDownManager.class)).shutdown(0, false);

        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, RELEASETIME);
        }
    }

    @Test
    public void testLaunchGrapevine() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/Grapevine_Simulator"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            DispatcherPro.main(new String[]{"DispatcherPro"});
            log.debug("started GrapevineSim");

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("DispatcherPro") != null;}, "window up");

            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("DispatcherPro version") != null;}, "first Info line seen");

            // now clean up frames, depending on what's actually left
            // DispatcherPro

            // gracefully shutdown, but don't exit
            ((DefaultShutDownManager)InstanceManager.getDefault(jmri.ShutDownManager.class)).shutdown(0, false);

        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, RELEASETIME);
            jmri.util.JUnitAppender.suppressWarnMessage("Timeout can't be handled due to missing node (index 1)");
            jmri.util.JUnitAppender.suppressWarnMessage("Timeout can't be handled due to missing node (index 0)");
        }
    }

    @Test
    @Ignore // Unreliable and causing too many false failures
    public void testLaunchTmcc() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/TMCC_Simulator"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            DispatcherPro.main(new String[]{"DispatcherPro"});
            log.debug("started TmccSim");

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("DispatcherPro") != null;},"window up");

            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("DispatcherPro version") != null;}, "first Info line seen");

            // now clean up frames, depending on what's actually left
            // DispatcherPro

            // gracefully shutdown, but don't exit
            ((DefaultShutDownManager)InstanceManager.getDefault(jmri.ShutDownManager.class)).shutdown(0, false);

        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, RELEASETIME);
        }
    }

//    @Test
//    public void testLaunchSprog() throws IOException {
//        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//
//        try {
//            // create a custom profile
//            File tempFolder = folder.newFolder();
//            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/Sprog_Simulator"), tempFolder);
//            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );
//
//            // launch!
//            DispatcherPro.main(new String[]{"DispatcherPro"});
//            log.debug("started SprogSim");
//
//            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("DispatcherPro") != null;}, "window up");
//
//            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("DispatcherPro version") != null;}, "first Info line seen");
//
//            // DispatcherPro
//        } finally {
//            // wait for threads, etc
//            jmri.util.JUnitUtil.releaseThread(this, RELEASETIME);
//        }
//    }

    @Test
    public void testLaunchInitLoop() throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
                
        try {
            // create a custom profile
            File tempFolder = folder.newFolder();
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/Prevent_Init_Loop"), tempFolder);
            System.setProperty("org.jmri.profile", tempFolder.getAbsolutePath() );

            // launch!
            DispatcherPro.main(new String[]{"DispatcherPro"});

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame("DispatcherPro") != null;}, "window up");
        
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith("DispatcherPro version") != null;}, "first Info line seen");


            // maybe have it run a script to indicate that it's really up?
            
            // now clean up frames, depending on what's actually left
            // DispatcherPro

            // gracefully shutdown, but don't exit
            ((DefaultShutDownManager)InstanceManager.getDefault(jmri.ShutDownManager.class)).shutdown(0, false);

        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, RELEASETIME);
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

    private final static Logger log = LoggerFactory.getLogger(DispatcherProTest.class);

}
