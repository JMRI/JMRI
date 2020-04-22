package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class YardmasterByTrackActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        YardmasterByTrackAction t = new YardmasterByTrackAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardmasterByTrackAction a = new YardmasterByTrackAction();
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, 0, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleYardmasterByTrack"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
    }

    // private final static Logger log = LoggerFactory.getLogger(YardmasterByTrackActionTest.class);

}
