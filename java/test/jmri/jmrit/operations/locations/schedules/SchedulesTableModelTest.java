package jmri.jmrit.operations.locations.schedules;

import jmri.jmrit.operations.OperationsTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SchedulesTableModelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SchedulesTableModel t = new SchedulesTableModel();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(SchedulesTableModelTest.class);

}
