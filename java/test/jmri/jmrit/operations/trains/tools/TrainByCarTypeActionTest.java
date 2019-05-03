package jmri.jmrit.operations.trains.tools;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.trains.Train;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainByCarTypeActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Train train1 = new Train("TESTTRAINID", "TESTTRAINNAME");
        TrainByCarTypeAction t = new TrainByCarTypeAction("Test Action",train1);
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainByCarTypeActionTest.class);

}
