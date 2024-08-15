package jmri.web.servlet.operations;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HtmlConductorTest {

    @Test
    public void testCTor() throws java.io.IOException {
        HtmlConductor t = new HtmlConductor(java.util.Locale.US,
                     (InstanceManager.getDefault(TrainManager.class)).getTrainById("2"));
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testLocation() throws java.io.IOException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById("2");
        train.build();
        HtmlConductor hc = new HtmlConductor(java.util.Locale.US,train);
        Assert.assertNotNull("exists", hc);
        String loc = hc.getLocation();
        Assert.assertTrue("location train name", loc.contains("SFF Train icon name"));
        Assert.assertFalse("location terminated", loc.contains("<h2>Terminated</h2>"));
        train.terminate();
        loc = hc.getLocation();
        Assert.assertTrue("location train name", loc.contains("SFF Train icon name"));
        Assert.assertTrue("location terminated", loc.contains("<h2>Terminated</h2>"));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initIdTagManager();
        jmri.util.JUnitOperationsUtil.setupOperationsTests();
        jmri.util.JUnitOperationsUtil.initOperationsData();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(HtmlConductorTest.class);

}
