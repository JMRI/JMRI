package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainByCarTypeActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainByCarTypeAction t = new TrainByCarTypeAction(train1);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainById("1");
        Assert.assertNotNull(train1);

        // create work to show
        Assert.assertTrue(train1.build());
        Assert.assertTrue(train1.isBuilt());
        TrainByCarTypeAction a = new TrainByCarTypeAction(train1);

        Thread performAction = new Thread(new Runnable() {
            @Override
            public void run() {
                a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        });
        performAction.setName("Test Action"); // NOI18N
        performAction.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return performAction.getState().equals(Thread.State.TERMINATED);
        }, "wait to complete");

        // confirm window is showing
        JmriJFrame frame = JmriJFrame.getFrame(Bundle.getMessage("MenuItemShowCarTypes") + " " + train1.getName());
        Assert.assertNotNull("exists", frame);

        JUnitUtil.dispose(frame);

        JUnitOperationsUtil.checkOperationsShutDownTask();

    }

    // private final static Logger log =
    // LoggerFactory.getLogger(TrainByCarTypeActionTest.class);

}
