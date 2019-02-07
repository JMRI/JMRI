package jmri.jmrit.operations.trains.tools;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ChangeDepartureTimesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ChangeDepartureTimesAction t = new ChangeDepartureTimesAction("Test Action");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ChangeDepartureTimesActionTest.class);

}
