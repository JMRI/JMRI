package jmri.jmrit.operations.trains;

import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainCsvManifestTest {

    @Test
    public void testCTor() {
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        TrainCsvManifest t = new TrainCsvManifest(train1);
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitOperationsUtil.resetOperationsManager();
        jmri.util.JUnitOperationsUtil.initOperationsData();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitOperationsUtil.resetOperationsManager();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCsvManifestTest.class.getName());
}
