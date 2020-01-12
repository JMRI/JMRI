package jmri.jmrit.operations.trains.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainsByCarTypeActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainsByCarTypeAction t = new TrainsByCarTypeAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsByCarTypeActionTest.class);

}
