package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SchedulesAndStagingActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SchedulesAndStagingAction t = new SchedulesAndStagingAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SchedulesAndStagingAction a = new SchedulesAndStagingAction();
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("MenuItemStagingSchedules"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(SchedulesByLoadActionTest.class);

}
