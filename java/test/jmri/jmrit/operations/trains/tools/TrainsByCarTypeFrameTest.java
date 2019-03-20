package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainsByCarTypeFrameTest extends OperationsTestCase{

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsByCarTypeFrame t = new TrainsByCarTypeFrame();
        t.initComponents("BoxCar");
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testTrainsByCarTypeFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // confirm that train default accepts Boxcars
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.newTrain("Test Train Name 2");
        Assert.assertTrue("accepts Boxcar 1", t.acceptsTypeName("Boxcar"));

        TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
        f.initComponents("Boxcar");

        // remove Boxcar from trains
        JemmyUtil.enterClickAndLeave(f.clearButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertFalse("accepts Boxcar 2", t.acceptsTypeName("Boxcar"));

        // now add Boxcar to trains
        JemmyUtil.enterClickAndLeave(f.setButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertTrue("accepts Boxcar 3", t.acceptsTypeName("Boxcar"));

        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsByCarTypeFrameTest.class);

}
