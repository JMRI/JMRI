package jmri.jmrit.operations.trains.schedules;

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
public class TrainsScheduleActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        TrainsScheduleAction t = new TrainsScheduleAction();
        Assert.assertNotNull("exists", t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainsScheduleAction a = new TrainsScheduleAction();
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleScheduleTrains"));
        Assert.assertNotNull("exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainsScheduleActionTest.class);

}
