package jmri.jmrit.operations.trains.manualtrainbuilder.gui;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Daniel Boudreau Copyright (C) 2028
 */
public class TrainManualBuildTableModelTest extends OperationsTestCase {

    @Test
    public void testCreate() {
        TrainManualBuildTableModel tmbi = new TrainManualBuildTableModel();
        Assert.assertNotNull("exists", tmbi);
    }
}
