package jmri.jmrit.operations.trains.manualtrainbuilder.gui;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2028
 */
public class TrainManualBuildEditFrameTest extends OperationsTestCase {

    @Test
    public void testCreate() {
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train train = trainManager.newTrain("Test");
        TrainManualBuildEditFrame tmbi = new TrainManualBuildEditFrame(train.getId());
        Assert.assertNotNull("exists", tmbi);
    }
}
