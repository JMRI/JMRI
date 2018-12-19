package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainManifestOptionFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManifestOptionFrame t = new TrainManifestOptionFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        TrainManifestOptionFrame t = new TrainManifestOptionFrame();
        Assert.assertNotNull("exists",t);
        
        t.initComponents(trainEditFrame);
        Assert.assertTrue(t.isShowing());
        
        t.railroadNameTextField.setText("test railroad name");
        JemmyUtil.enterClickAndLeave(t.saveButton);
        
        Assert.assertEquals("test railroad name", train.getRailroadName());
        
        // closing the train edit window should also close the option window
        JUnitUtil.dispose(trainEditFrame);
        Assert.assertFalse(t.isShowing());
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainManifestOptionFrameTest.class);

}
