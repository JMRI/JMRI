package jmri.jmrit.operations.trains;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainManifestTest {

    @Test
    public void testCTor() {
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        TrainManifest t = new TrainManifest(train1);
        Assert.assertNotNull("exists", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitOperationsUtil.resetOperationsManager();
        jmri.util.JUnitOperationsUtil.initOperationsData();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainManifestTest.class);
}
