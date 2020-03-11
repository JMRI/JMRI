package jmri.jmrit.operations.trains;

import org.junit.Assert;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainCsvManifestTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        JUnitOperationsUtil.initOperationsData();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Assert.assertTrue(train1.build());
        TrainCsvManifest t = new TrainCsvManifest(train1);
        Assert.assertNotNull("exists", t);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCsvManifestTest.class);
}
