package jmri.jmrit.operations.routes;

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
public class RoutesTableActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RoutesTableAction t = new RoutesTableAction();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RoutesTableAction a = new RoutesTableAction();
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        JmriJFrame f = JmriJFrame.getFrame(Bundle.getMessage("TitleRoutesTable"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
}
