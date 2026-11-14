package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.nio.file.Files;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class OptionsFileTest {

    @Test
    public void testCTor() {
        OptionsFile t = new OptionsFile();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSetOptionsFileNameAndRead() {
        // The Dispatcher functionality is tightly coupled to the Dispatcher
        // Frame.  As a result, we have can currently only test reading the
        // options file by creating a DispatcherFrame object.  A future
        // enhancement shold probably break this coupling.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // test some options from file
        Assert.assertTrue("File AutoTurnouts", d.getAutoTurnouts());
        Assert.assertTrue("File HasOccupancyDetection", d.getHasOccupancyDetection());
        // Find the window by name and close it.
        (new org.netbeans.jemmy.operators.JFrameOperator(Bundle.getMessage("TitleDispatcher"))).requestClose();
        JUnitUtil.dispose(d);

    }

    @Test
    public void testWaitWhereTrainsCanStopIsOffUnlessSavedOn(@TempDir File tempDir) throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // a copy of an options file written before the option existed
        File file = new File(tempDir, "dispatcheroptions.xml");
        Files.copy(new File("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml").toPath(), file.toPath());
        OptionsFile.setDefaultFileName(file.getPath());
        try {
            DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
            Assert.assertFalse("off when the file does not say", d.getWaitWhereTrainsCanStop());

            OptionsFile options = InstanceManager.getDefault(OptionsFile.class);
            d.setWaitWhereTrainsCanStop(true);
            options.writeDispatcherOptions(d);
            d.setWaitWhereTrainsCanStop(false);
            options.readDispatcherOptions(d);
            Assert.assertTrue("on when saved on", d.getWaitWhereTrainsCanStop());

            d.setWaitWhereTrainsCanStop(false);
            options.writeDispatcherOptions(d);
            d.setWaitWhereTrainsCanStop(true);
            options.readDispatcherOptions(d);
            Assert.assertFalse("off when saved off", d.getWaitWhereTrainsCanStop());

            (new org.netbeans.jemmy.operators.JFrameOperator(Bundle.getMessage("TitleDispatcher"))).requestClose();
            JUnitUtil.dispose(d);
        } finally {
            OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(OptionsFileTest.class);

}
