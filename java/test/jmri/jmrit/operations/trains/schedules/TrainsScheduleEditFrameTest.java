package jmri.jmrit.operations.trains.schedules;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainsScheduleEditFrameTest extends OperationsTestCase {

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
        JemmyUtil.enterClickAndLeave(f.addButton);

        Assert.assertNotNull("Train schedule manager exists", tsm);
        Assert.assertNotNull("A new Day schedule exists", tsm.getScheduleByName("A New Day"));

        JemmyUtil.enterClickAndLeave(f.deleteButton);

        Assert.assertNull("A new Day schedule does not exist", tsm.getScheduleByName("A New Day"));

        JemmyUtil.enterClickAndLeave(f.replaceButton);

        Assert.assertNotNull("A new Day schedule exists", tsm.getScheduleByName("A New Day"));

        JUnitUtil.dispose(f);
    }
    
    // private final static Logger log = LoggerFactory.getLogger(TrainsScheduleEditFrameTest.class);

}
