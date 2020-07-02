package jmri.jmrit.operations.locations.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.jupiter.api.*;

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
