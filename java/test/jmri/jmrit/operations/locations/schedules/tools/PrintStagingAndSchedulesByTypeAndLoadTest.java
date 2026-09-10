package jmri.jmrit.operations.locations.schedules.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Daniel Boudreau Copyright (C) 2026
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class PrintStagingAndSchedulesByTypeAndLoadTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SchedulesAndStagingFrame ssf = new SchedulesAndStagingFrame();
        PrintStagingAndSchedulesByTypeAndLoad t = new PrintStagingAndSchedulesByTypeAndLoad(true, ssf);
        Assert.assertNotNull("exists", t);
    }
}
