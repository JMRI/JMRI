package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class ScheduleCopyFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ScheduleCopyFrame t = new ScheduleCopyFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testCopy() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ScheduleManager sManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule sch = sManager.newSchedule("Test Schedule A");
        Assert.assertNotNull("Test Schedule A exists", sch);
        
        // add some schedule items to copy
        sch.addItem("Boxcar");
        sch.addItem("Tank car");

        ScheduleCopyFrame scf = new ScheduleCopyFrame(sch);
        Assert.assertNotNull("exists", scf);

        // no new schedule name
        JemmyUtil.enterClickAndLeaveThreadSafe(scf.copyButton);
        Assert.assertEquals("schedules 1", 1, sManager.getSchedulesByNameList().size());
        JemmyUtil.pressDialogButton(scf, MessageFormat.format(Bundle
                .getMessage("CanNotSchedule"), new Object[]{Bundle.getMessage("ButtonCopy")}),
                Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(scf);

        // name too long
        scf.scheduleNameTextField.setText("abcdefghijklmnopqrstuvwxwyz");
        JemmyUtil.enterClickAndLeaveThreadSafe(scf.copyButton);
        Assert.assertEquals("schedules 1a", 1, sManager.getSchedulesByNameList().size());
        JemmyUtil.pressDialogButton(scf, MessageFormat.format(Bundle
                .getMessage("CanNotSchedule"), new Object[]{Bundle.getMessage("ButtonCopy")}),
                Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(scf);
        
        // good schedule name
        scf.scheduleNameTextField.setText("TestCopyScheduleName");
        JemmyUtil.enterClickAndLeave(scf.copyButton);
        Assert.assertEquals("schedules 2", 2, sManager.getSchedulesByNameList().size());
        Assert.assertNotNull("Schedule exists", sManager.getScheduleByName("TestCopyScheduleName"));
        
        // same schedule name, error
        JemmyUtil.enterClickAndLeave(scf.copyButton);
        Assert.assertEquals("schedules 2", 2, sManager.getSchedulesByNameList().size());
        JemmyUtil.pressDialogButton(scf, MessageFormat.format(Bundle
                .getMessage("CanNotSchedule"), new Object[]{Bundle.getMessage("ButtonCopy")}),
                Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(scf);
        JUnitUtil.dispose(scf);
    }
}
