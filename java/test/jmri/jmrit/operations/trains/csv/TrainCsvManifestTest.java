package jmri.jmrit.operations.trains.csv;

import java.io.*;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainCsvManifestTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        JUnitOperationsUtil.initOperationsData();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Assert.assertTrue(train1.build());
        try {
            TrainCsvManifest t = new TrainCsvManifest(train1);
            Assert.assertNotNull("exists", t);
        } catch (Exception e) {
            Assert.fail();
        }

        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    @Test
    public void testCreateCsvManifest() throws IOException {
        JUnitOperationsUtil.initOperationsData();
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainById("1");
        Setup.setGenerateCsvManifestEnabled(true);
        Assert.assertTrue(train1.build());
        File file = train1.createCsvManifestFile();
        Assert.assertNotNull("exists", file);

        BufferedReader in = JUnitOperationsUtil.getBufferedReader(file);
        Assert.assertEquals("confirm number of lines in manifest", 39, in.lines().count());
        in.close();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCsvManifestTest.class);
}
