package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
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
public class YardmasterFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        YardmasterFrame t = new YardmasterFrame(l);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testLocationWithWork() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        // build a train to create data for the Yardmaster window
        Train train = InstanceManager.getDefault(TrainManager.class).getTrainByName("STF");
        Assert.assertNotNull("train must exist", train);
        Assert.assertTrue("successful build", train.build());

        Location l = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");
        Assert.assertNotNull("location must exist", l);

        YardmasterFrame ymFrame = new YardmasterFrame(l);
        Assert.assertNotNull("exists", ymFrame);

        YardmasterPanel ymPanel = (YardmasterPanel) ymFrame.getContentPane();

        // press "Next" button to load window
        JemmyUtil.enterClickAndLeave(ymPanel.nextButton);

        // exercise buttons
        JemmyUtil.enterClickAndLeave(ymPanel.selectButton);

        JemmyUtil.enterClickAndLeave(ymPanel.clearButton);

        // press "Modify"
        JemmyUtil.enterClickAndLeave(ymPanel.modifyButton);

        // clear dialog window
        JemmyUtil.pressDialogButton(ymFrame, Bundle.getMessage("AddCarsToTrain?"), Bundle.getMessage("ButtonNo"));

        // press "Done"
        JemmyUtil.enterClickAndLeave(ymPanel.modifyButton);

        JUnitUtil.dispose(ymFrame);
    }

    // private final static Logger log = LoggerFactory.getLogger(YardmasterFrameTest.class);
}
