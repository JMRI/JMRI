package jmri.jmrit.operations.trains.excel;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainCustomManifestTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainCustomManifest t = new TrainCustomManifest();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testFileExists() {
        TrainCustomManifest t = new TrainCustomManifest();
        Assert.assertFalse("file should not exist", t.doesCommonFileExist());
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCheckProcessReady() {
        TrainCustomManifest t = new TrainCustomManifest();
        Assert.assertTrue("should be ready", t.checkProcessReady());
    }
    
    @Test
    public void testProcess() {
        TrainCustomManifest t = new TrainCustomManifest();
        Assert.assertFalse("should return false", t.process());
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainCustomManifestTest.class);

}
