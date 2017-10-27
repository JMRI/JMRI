package jmri.jmrit.operations.trains.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintTrainBuildReportActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tef = new TrainEditFrame(train1);
        PrintTrainBuildReportAction t = new PrintTrainBuildReportAction("Test Action",true,tef);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(tef);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintTrainBuildReportActionTest.class);

}
