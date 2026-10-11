package jmri.jmrit.operations.locations.schedules.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Daniel Boudreau Copyright (C) 2026
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class PrintStagingAndSchedulesByTypeAndLoadActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SchedulesAndStagingFrame ssf = new SchedulesAndStagingFrame();
        PrintStagingAndSchedulesByTypeAndLoadAction t = new PrintStagingAndSchedulesByTypeAndLoadAction(true, ssf);
        Assert.assertNotNull("exists", t);
    }
}
