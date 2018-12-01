package apps;

import java.awt.GraphicsEnvironment;
import java.io.*;

import org.apache.commons.io.*;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.junit.rules.TemporaryFolder;
import jmri.InstanceManager;
import jmri.profile.ProfileManager;
import jmri.managers.DefaultShutDownManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.JUnitAppender;
import jmri.util.junit.rules.RetryRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base implementation for a test that launches and tests complete JMRI apps from prebuilt profile directories
 *
 * @author Bob Jacobsen 2018
 */
abstract public class LaunchJmriAppBase {

    static final int RELEASETIME = 3000;  // mSec
    static final int TESTMAXTIME = 20;    // seconds - not too long, so job doesn't hang

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public Timeout globalTimeout = Timeout.seconds(TESTMAXTIME);

    @Rule
    public RetryRule retryRule = new RetryRule(1); // allow 1 retry

    /**
     * Run one application
     * @param Name of the Profile to copy from files in java/test/apps/PanelPro/profiles/
     * @param Name application (frame) title 
     * @param Start of the "we're up!" message
     */
    protected void runOne(String profileName, String frameName, String startMessageStart) throws IOException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
                
        try {

            // create a custom profile
            File tempFolder = folder.newFolder();
            File profileDir = new File (tempFolder.getAbsolutePath()+File.separator+profileName);
            profileDir.mkdir();
            System.setProperty("jmri.prefsdir", tempFolder.getAbsolutePath() );
            FileUtils.copyDirectory(new File("java/test/apps/PanelPro/profiles/"+profileName), profileDir);
            System.setProperty("org.jmri.profile", profileDir.getAbsolutePath() );

            // launch!
            launch(new String[]{profileName});

            JUnitUtil.waitFor(()->{return JmriJFrame.getFrame(frameName) != null;}, "window up");
        
            JUnitUtil.waitFor(()->{return JUnitAppender.checkForMessageStartingWith(startMessageStart) != null;}, "first Info line seen");

            extraChecks();
            
            // maybe have it run a script to indicate that it's really up?
            
            // now clean up frames, depending on what's actually left
            cleanup();
            
            // gracefully shutdown, but don't exit
            ((DefaultShutDownManager)InstanceManager.getDefault(jmri.ShutDownManager.class)).shutdown(0, false);

        } finally {
            // wait for threads, etc
            jmri.util.JUnitUtil.releaseThread(this, RELEASETIME);
        }
    }

    abstract protected void launch(String[] args);
    
    protected void extraChecks() {}
    protected void cleanup() {}
    
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

}
