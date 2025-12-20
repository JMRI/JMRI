package jmri.web.servlet.operations;

import org.junit.jupiter.api.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class HtmlConductorTest {

    @Test
    public void testCTor() throws java.io.IOException {
        HtmlConductor t = new HtmlConductor(java.util.Locale.US,
                     (InstanceManager.getDefault(TrainManager.class)).getTrainById("2"));
        assertNotNull( t, "exists");
    }

    @Test
    public void testLocation() throws java.io.IOException {
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainById("2");
        train.build();
        HtmlConductor hc = new HtmlConductor(java.util.Locale.US,train);
        assertNotNull( hc, "exists");
        String loc = hc.getLocation();
        assertTrue( loc.contains("SFF Train icon name"), "location train name");
        assertFalse( loc.contains("<h2>Terminated</h2>"), "location terminated");
        train.terminate();
        loc = hc.getLocation();
        assertTrue( loc.contains("SFF Train icon name"), "location train name");
        assertTrue( loc.contains("<h2>Terminated</h2>"), "location terminated");
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

    // private final static Logger log = LoggerFactory.getLogger(HtmlConductorTest.class);

}
