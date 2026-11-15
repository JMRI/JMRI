package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AllocatedSectionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");  // exist?
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        jmri.Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain at = new ActiveTrain(transit, "Train", ActiveTrain.USER);
        jmri.Section section1 = new jmri.implementation.DefaultSection("TS1");
        jmri.Section section2 = new jmri.implementation.DefaultSection("TS2");
        AllocatedSection t = new AllocatedSection(section1, at, 1, section2, 2);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(d);
    }

    @Test
    public void testSectionChangeMakesNoDispatcher() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        OptionsFile.setDefaultFileName("java/test/jmri/jmrit/dispatcher/dispatcheroptions.xml");
        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);
        jmri.Transit transit = new jmri.implementation.DefaultTransit("TT1");
        ActiveTrain run = new ActiveTrain(transit, "Run", ActiveTrain.USER);
        run.setDispatcher(d);
        ActiveTrain noDispatcher = new ActiveTrain(transit, "Other", ActiveTrain.USER);
        jmri.Section section1 = new jmri.implementation.DefaultSection("TS1");
        jmri.Section section2 = new jmri.implementation.DefaultSection("TS2");
        AllocatedSection forRun = new AllocatedSection(section1, run, 1, section2, 2);
        AllocatedSection forOther = new AllocatedSection(section2, noDispatcher, 1, null, 0);
        Assert.assertNotNull(forRun);
        Assert.assertNotNull(forOther);

        // the Dispatcher goes, as when a test ends, and then its Sections change,
        // as when the allocation thread finishes a release after it was disposed
        JUnitUtil.dispose(d);
        InstanceManager.deregister(d, DispatcherFrame.class);
        section1.setState(jmri.Section.FORWARD);
        section2.setState(jmri.Section.FORWARD);
        Assert.assertFalse("no new Dispatcher made", InstanceManager.containsDefault(DispatcherFrame.class));
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

    // private static final Logger log = LoggerFactory.getLogger(AllocatedSectionTest.class);
}
