package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainLoadOptionsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainEditFrame tf = new TrainEditFrame(train1);
        TrainLoadOptionsAction t = new TrainLoadOptionsAction("Test Action", tf);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(tf);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainLoadOptionsActionTest.class);
}
