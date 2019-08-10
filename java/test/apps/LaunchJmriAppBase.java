package apps;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.MockShutDownManager;
import jmri.util.junit.rules.RetryRule;

/**
 * Base implementation for a test that launches and tests complete JMRI apps
 * from prebuilt profile directories.
 *
 * @author Bob Jacobsen 2018
 */
abstract public class LaunchJmriAppBase {

    static final int RELEASETIME = 3000; // mSec
    static final int TESTMAXTIME = 40; // seconds - not too long, so job doesn't hang
    // in particular the #testLaunchInitLoop() test needs this time for setup

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public Timeout globalTimeout = Timeout.seconds(TESTMAXTIME);

    @Rule
    public RetryRule retryRule = new RetryRule(1); // allow 1 retry

    /**
     * Run one application.
     * 
     * @param profileName       Name of the Profile folder to copy from
     *                          java/test/apps/PanelPro/profiles/
     * @param frameName         Application (frame) title
     * @param startMessageStart Start of the "we're up!" message as seen in System Console
     */
    protected void runOne(String profileName, String frameName, String startMessageStart) throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        try {
            // create a custom profile
            folder.create();
            File tempFolder = folder.newFolder();
            File profileDir = new File(tempFolder.getAbsolutePath() + File.separator + profileName);
            profileDir.mkdir();
            System.setProperty("jmri.prefsdir", tempFolder.getAbsolutePath());
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/" + profileName), profileDir);
            System.setProperty("org.jmri.profile", profileDir.getAbsolutePath());

            // launch!
            launch(new String[]{profileName});

            JUnitUtil.waitFor(() -> {
                return JmriJFrame.getFrame(frameName) != null;
            }, "the application window is up");

            JUnitUtil.waitFor(() -> {
                return JUnitAppender.checkForMessageStartingWith(startMessageStart) != null;
            }, "first Info line seen in Console after startup");

            extraChecks();

            // maybe have it run a script to indicate that it's really up?

            // now clean up frames, depending on what's actually left
            cleanup();

            // gracefully shutdown, but don't exit
            ShutDownManager sdm = InstanceManager.getDefault(ShutDownManager.class);
            if (sdm instanceof MockShutDownManager) {
                // ShutDownManagers other than MockShutDownManager really shutdown
                sdm.shutdown();
            }

        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, RELEASETIME);
        }
    }

    abstract protected void launch(String[] args);

    protected void extraChecks() {
    }

    protected void cleanup() {
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.clearShutDownManager();
        JUnitUtil.resetApplication();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

}
