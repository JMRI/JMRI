package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ShowCarsInTrainActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        ShowCarsInTrainAction t = new ShowCarsInTrainAction("Test Action", train1);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void performAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // create work to show
        Assert.assertTrue(train1.build());
        Assert.assertTrue(train1.isBuilt());
        
        ShowCarsInTrainAction pa = new ShowCarsInTrainAction("Test Action", train1);
        
        Thread performAction = new Thread(new Runnable() {
            @Override
            public void run() {
                pa.actionPerformed(new ActionEvent("test event", 0, null));
            }
        });
        performAction.setName("Test Action"); // NOI18N
        performAction.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return performAction.getState().equals(Thread.State.TERMINATED);
        }, "wait to complete");
        
        // confirm window is showing
        JmriJFrame frame = JmriJFrame.getFrame(Bundle.getMessage("TitleShowCarsInTrain")+ " (" + train1.getName() + ")");
        Assert.assertNotNull("exists", frame);
        
        JUnitUtil.dispose(frame);
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowCarsInTrainActionTest.class);

}
