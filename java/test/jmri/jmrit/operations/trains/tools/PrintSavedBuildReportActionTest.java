package jmri.jmrit.operations.trains.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintSavedBuildReportActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        PrintSavedBuildReportAction t = new PrintSavedBuildReportAction(true, train1);
        Assert.assertNotNull("exists", t);
    }

}
