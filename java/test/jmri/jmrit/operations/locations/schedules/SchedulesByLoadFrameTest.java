package jmri.jmrit.operations.locations.schedules;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SchedulesByLoadFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.util.JUnitOperationsUtil.initOperationsData();
        SchedulesByLoadFrame t = new SchedulesByLoadFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);

    }
    
    @Test
    public void testSchedulesByLoadFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SchedulesByLoadFrame f = new SchedulesByLoadFrame();
        Assert.assertNotNull(f);

        // TODO improve test
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(SchedulesByLoadFrameTest.class);

}
