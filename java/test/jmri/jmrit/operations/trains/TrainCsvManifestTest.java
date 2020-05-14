package jmri.jmrit.operations.trains;

import java.io.BufferedReader;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
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
    
    @Test
    public void testCreateCsvManifest() {
        JUnitOperationsUtil.initOperationsData();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Setup.setGenerateCsvManifestEnabled(true);
        Assert.assertTrue(train1.build());
        File file = train1.createCSVManifestFile();
        Assert.assertNotNull("exists", file);
        
        BufferedReader in = JUnitOperationsUtil.getBufferedReader(file);
        Assert.assertEquals("confirm number of lines in manifest", 39, in.lines().count());
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCsvManifestTest.class);
}
