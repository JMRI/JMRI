package jmri.web.servlet.operations;

import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HtmlManifestTest {

    @Test
    public void testCTor() throws java.io.IOException {
        HtmlManifest t = new HtmlManifest(java.util.Locale.US,
                     (InstanceManager.getDefault(TrainManager.class)).getTrainById("2"));
        assertNotNull( t, "exists");
    }

    @Test
    public void testLocations() throws java.io.IOException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById("2");
        train.build();
        HtmlManifest hc = new HtmlManifest(java.util.Locale.US, train);
        assertNotNull( hc, "exists");
        String loc = hc.getLocations();
        assertTrue( loc.contains("Scheduled work at North End Staging"), "departure location");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initIdTagManager();
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
