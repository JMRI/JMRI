package jmri.jmrit.operations.trains.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

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

}
