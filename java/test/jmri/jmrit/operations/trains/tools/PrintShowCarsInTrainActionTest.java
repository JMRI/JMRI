package jmri.jmrit.operations.trains.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class PrintShowCarsInTrainActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        PrintShowCarsInTrainAction t = new PrintShowCarsInTrainAction(true, train1);
        Assert.assertNotNull("exists", t);
    }
}
