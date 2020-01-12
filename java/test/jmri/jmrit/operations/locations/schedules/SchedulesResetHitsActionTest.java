package jmri.jmrit.operations.locations.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SchedulesResetHitsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SchedulesResetHitsAction t = new SchedulesResetHitsAction("Test");
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(SchedulesResetHitsActionTest.class);

}
