package jmri.jmrit.operations.trains.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsScheduleEditActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainsScheduleEditAction t = new TrainsScheduleEditAction();
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsScheduleEditActionTest.class);

}
