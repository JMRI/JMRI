package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;
import jmri.util.JUnitUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainScriptActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tef = new TrainEditFrame(train1);
        TrainScriptAction t = new TrainScriptAction(tef);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(tef);
    }

    // private final static Logger log =
    // LoggerFactory.getLogger(TrainScriptActionTest.class);

}
