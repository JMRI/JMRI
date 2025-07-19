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
public class HtmlManifestTest {

    @Test
    public void testCTor() throws java.io.IOException {
        HtmlManifest t = new HtmlManifest(java.util.Locale.US,
                     (InstanceManager.getDefault(TrainManager.class)).getTrainById("2"));
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testLocations() throws java.io.IOException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById("2");
        train.build();
        HtmlManifest hc = new HtmlManifest(java.util.Locale.US, train);
        Assert.assertNotNull("exists", hc);
        String loc = hc.getLocations();
        Assert.assertTrue("departure location", loc.contains("Scheduled work at North End Staging"));
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

    // private final static Logger log = LoggerFactory.getLogger(HtmlManifestTest.class);

}
