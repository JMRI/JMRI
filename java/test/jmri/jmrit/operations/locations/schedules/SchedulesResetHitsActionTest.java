package jmri.jmrit.operations.locations.schedules;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SchedulesResetHitsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SchedulesResetHitsAction t = new SchedulesResetHitsAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(SchedulesResetHitsActionTest.class);

}
