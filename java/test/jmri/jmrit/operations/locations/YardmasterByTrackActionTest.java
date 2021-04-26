package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class YardmasterByTrackActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Location location = JUnitOperationsUtil.createOneNormalLocation("Test");
        YardmasterByTrackAction t = new YardmasterByTrackAction(location);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location location = JUnitOperationsUtil.createOneNormalLocation("Test");
        YardmasterByTrackAction a = new YardmasterByTrackAction(location);
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleYardmasterByTrack") + " (Test)");
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(YardmasterByTrackActionTest.class);

}
