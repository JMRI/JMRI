package jmri.jmrit.operations.trains.gui;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.util.JUnitUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainEditBuildOptionsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tf = new TrainEditFrame(train1);
        TrainEditBuildOptionsAction t = new TrainEditBuildOptionsAction(tf);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(tf);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(TrainEditBuildOptionsActionTest.class);
}
