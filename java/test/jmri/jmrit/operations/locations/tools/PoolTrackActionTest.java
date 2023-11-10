package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PoolTrackActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame tf = new YardEditFrame();
        PoolTrackAction t = new PoolTrackAction(tf);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("LOC1", "Location One");
        Track t = new Track("ID1", "TestTrack1", Track.YARD, l);
        YardEditFrame tf = new YardEditFrame();
        tf.initComponents(t);
        PoolTrackAction a = new PoolTrackAction(tf);
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("MenuItemPoolTrack"));
        Assert.assertNotNull("frame exists", f);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        // improve test coverage by requesting again
        f = JmriJFrame.getFrame(Bundle.getMessage("MenuItemPoolTrack"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
        
        // confirm only one frame existed, the other should have been disposed
        f = JmriJFrame.getFrame(Bundle.getMessage("MenuItemPoolTrack"));
        Assert.assertNull("frame gone", f);
    }

    // private final static Logger log = LoggerFactory.getLogger(PoolTrackActionTest.class);

}
