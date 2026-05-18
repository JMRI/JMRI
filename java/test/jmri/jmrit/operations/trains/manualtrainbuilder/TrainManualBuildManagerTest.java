package jmri.jmrit.operations.trains.manualtrainbuilder;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2028
 */
public class TrainManualBuildManagerTest extends OperationsTestCase {

    @Test
    public void testCreate() {
        TrainManualBuildManager tmbm = new TrainManualBuildManager();
        Assert.assertNotNull("exists", tmbm);
    }

    @Test
    public void testNumEntries() {
        TrainManualBuildManager tmbm = new TrainManualBuildManager();
        Assert.assertEquals(0, tmbm.numEntries());
    }
}
