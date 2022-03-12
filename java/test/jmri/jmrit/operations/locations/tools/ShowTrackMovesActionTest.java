package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ShowTrackMovesActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ShowTrackMovesAction t = new ShowTrackMovesAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ShowTrackMovesAction a = new ShowTrackMovesAction();
        Assert.assertNotNull("exists", a);
        
        // default
        Assert.assertFalse("confirm default", Setup.isShowTrackMovesEnabled());
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        Assert.assertTrue("confirm change", Setup.isShowTrackMovesEnabled());
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowTrackMovesActionTest.class);

}
