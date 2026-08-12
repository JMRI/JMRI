package jmri.jmrit.operations.trains.manualtrainbuilder;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2028
 */
public class TrainManualBuildTest extends OperationsTestCase {

    @Test
    public void testCreate() {
        TrainManualBuild tmb = new TrainManualBuild("3", "5");
        Assert.assertNotNull("exists", tmb);
        Assert.assertEquals("id", "3", tmb.getId());
        Assert.assertEquals("trainId", "5", tmb.getTrainId());
    }
}
