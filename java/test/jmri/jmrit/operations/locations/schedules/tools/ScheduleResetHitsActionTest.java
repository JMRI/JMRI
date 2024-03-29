package jmri.jmrit.operations.locations.schedules.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.schedules.Schedule;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ScheduleResetHitsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ScheduleResetHitsAction t = new ScheduleResetHitsAction(new Schedule("Test id", "Test Name"));
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ScheduleResetHitsActionTest.class);

}
