package jmri.jmrit.operations.trains.manualtrainbuilder;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2028
 */
public class TrainManualBuildItemTest extends OperationsTestCase {

    @Test
    public void testCreate() {
        TrainManualBuildItem tmbi = new TrainManualBuildItem("2");
        Assert.assertNotNull("exists", tmbi);
        Assert.assertEquals("2", tmbi.getId());
    }
}
