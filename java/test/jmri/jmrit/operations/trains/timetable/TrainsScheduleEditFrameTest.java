package jmri.jmrit.operations.trains.timetable;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainsScheduleEditFrameTest extends OperationsSwingTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleEditFrame t = new TrainsScheduleEditFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testTrainsScheduleEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleEditFrame f = new TrainsScheduleEditFrame();
        TrainScheduleManager tsm = InstanceManager.getDefault(TrainScheduleManager.class);
        Assert.assertNotNull("frame exists", f);

        f.addTextBox.setText("A New Day");
        enterClickAndLeave(f.addButton);

        Assert.assertNotNull("Train schedule manager exists", tsm);
        Assert.assertNotNull("A new Day schedule exists", tsm.getScheduleByName("A New Day"));

        enterClickAndLeave(f.deleteButton);

        Assert.assertNull("A new Day schedule does not exist", tsm.getScheduleByName("A New Day"));

        enterClickAndLeave(f.replaceButton);

        Assert.assertNotNull("A new Day schedule exists", tsm.getScheduleByName("A New Day"));

        JUnitUtil.dispose(f);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsScheduleEditFrameTest.class);

}
