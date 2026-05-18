package jmri.jmrit.operations.trains.manualtrainbuilder.gui;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2028
 */
public class TrainManualBuildActionTest extends OperationsTestCase {

    @Test
    public void testCreate() {
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train train = trainManager.newTrain("Test");
        TrainManualBuildAction tmba = new TrainManualBuildAction(train);
        Assert.assertNotNull("exists", tmba);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train train = trainManager.newTrain("Test");
        TrainManualBuildAction tmba = new TrainManualBuildAction(train);
        
        tmba.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleManualBuild"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }
}
