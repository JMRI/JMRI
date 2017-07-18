package jmri.jmrit.operations.trains.tools;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainEditFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintTrainsByCarTypesActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tef = new TrainEditFrame(train1);
        jmri.util.JmriJFrame jf = new jmri.util.JmriJFrame("Print Trains by Car Types Action");
        PrintTrainsByCarTypesAction t = new PrintTrainsByCarTypesAction("Test Action",jf,true,tef);
        Assert.assertNotNull("exists",t);
        tef.dispose();
        jf.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(PrintTrainsByCarTypesActionTest.class.getName());

}
