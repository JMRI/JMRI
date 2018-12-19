package jmri.jmrit.operations.locations.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SchedulesByLoadActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SchedulesByLoadAction t = new SchedulesByLoadAction("Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(SchedulesByLoadActionTest.class);

}
