package jmri.jmrit.operations.trains.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainsScheduleTableModelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainsScheduleTableModel t = new TrainsScheduleTableModel();
        Assert.assertNotNull("exists", t);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsScheduleTableModelTest.class);

}
