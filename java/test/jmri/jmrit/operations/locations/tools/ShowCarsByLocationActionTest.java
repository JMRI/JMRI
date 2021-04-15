package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

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
public class ShowCarsByLocationActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ShowCarsByLocationAction t = new ShowCarsByLocationAction(true, null, null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ShowCarsByLocationAction a = new ShowCarsByLocationAction(true, null, null);
        Assert.assertNotNull("exists", a);
        
        a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
        
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
        
        JmriJFrame f = JmriJFrame.getFrame(rb.getString("TitleCarsTable"));
        Assert.assertNotNull("frame exists", f);
        JUnitUtil.dispose(f);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowCarsByLocationActionTest.class);

}
