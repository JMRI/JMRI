package jmri.jmrit.operations.locations.schedules.tools;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 * @author Paul Bender Copyright (C) 2017
 * @author Daniel Boudreau Copyright (C) 2026
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class PrintSchedulesByTypeAndLoadTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintSchedulesByTypeAndLoad t = new PrintSchedulesByTypeAndLoad(true, "type", "load");
        Assert.assertNotNull("exists", t);
    }
}
