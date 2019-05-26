package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class SetPhysicalLocationActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Location l = new Location("Test id", "Test Name");
        SetPhysicalLocationAction t = new SetPhysicalLocationAction("Test",l);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Location ni = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");
        Assert.assertNotNull("exists", ni);
        SetPhysicalLocationAction spla = new SetPhysicalLocationAction("Test", ni);
        Assert.assertNotNull("exists", spla);
        spla.actionPerformed(new ActionEvent("Test Action", 0, null));
        // confirm window exists
        JmriJFrame plf = JmriJFrame.getFrame(Bundle.getMessage("MenuSetPhysicalLocation"));
        Assert.assertNotNull("exists", plf);
        JUnitUtil.dispose(plf);
    }
    
    @Test
    public void testButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Location ni = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");
        Assert.assertNotNull("exists", ni);
        SetPhysicalLocationAction spla = new SetPhysicalLocationAction("Test", ni);
        Assert.assertNotNull("exists", spla);
        spla.actionPerformed(new ActionEvent("Test Action", 0, null));
        // confirm window exists
        JmriJFrame plf = JmriJFrame.getFrame(Bundle.getMessage("MenuSetPhysicalLocation"));
        Assert.assertNotNull("exists", plf);
        
        SetPhysicalLocationFrame splf = (SetPhysicalLocationFrame)plf;
        
        // test save button
        JemmyUtil.enterClickAndLeave(splf.saveButton);
        
        // should cause a popup window to appear
        JemmyUtil.pressDialogButton(Bundle.getMessage("UpdateDefaults"), Bundle.getMessage("ButtonNo"));
        
        // test close button
        JemmyUtil.enterClickAndLeave(splf.closeButton);
        
        JUnitUtil.dispose(plf);
        
        log.debug("test done");
    }

     private final static Logger log = LoggerFactory.getLogger(SetPhysicalLocationActionTest.class);

}
