package jmri.jmrit.operations.trains.tools;

import org.junit.Assert;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ExportTrainRosterActionTest extends OperationsTestCase {
    @Test
    public void testCTor() {
        ExportTimetableAction t = new ExportTimetableAction();
        Assert.assertNotNull("exists",t);
    }

    // private final static Logger log = LoggerFactory.getLogger(ExportTrainRosterActionTest.class);

}
