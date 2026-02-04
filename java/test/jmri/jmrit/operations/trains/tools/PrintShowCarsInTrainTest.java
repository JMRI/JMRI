package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.*;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class PrintShowCarsInTrainTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintShowCarsInTrain t = new PrintShowCarsInTrain();
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testPreviewTrain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        train.build();
        
        PrintShowCarsInTrain p = new PrintShowCarsInTrain();
        p.printCarsInTrain(train, true);
        
        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " + Bundle.getMessage("TitleShowCarsInTrain", train.getName()));
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testPreviewRoute() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        train.build();
        
        PrintShowCarsInTrain p = new PrintShowCarsInTrain();
        p.printCarsInTrainRoute(train, true);
        
        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame = JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") +
                " " + Bundle.getMessage("TitleShowCarsInTrain", train.getName()));
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

}
