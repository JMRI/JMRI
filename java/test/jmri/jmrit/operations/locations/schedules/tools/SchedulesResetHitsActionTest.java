package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.schedules.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class SchedulesResetHitsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        SchedulesResetHitsAction t = new SchedulesResetHitsAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        SchedulesResetHitsAction a = new SchedulesResetHitsAction();
        Assert.assertNotNull("exists", a);
        
        // create a schedule with hits
        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        Schedule sch = sm.newSchedule("Test Hit Schedule");
        ScheduleItem si = sch.addItem("Boxcar");
        si.setHits(34);
        
        Assert.assertEquals("confirm hit count", 34, si.getHits());
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        Assert.assertEquals("confirm hit count", 0, si.getHits());
    }

    // private final static Logger log = LoggerFactory.getLogger(SchedulesResetHitsActionTest.class);

}
