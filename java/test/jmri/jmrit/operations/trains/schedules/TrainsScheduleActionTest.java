package jmri.jmrit.operations.trains.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsScheduleActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainsScheduleAction t = new TrainsScheduleAction("Test Action");
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsScheduleActionTest.class);

}
