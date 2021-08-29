package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocationCopyFrameTest  extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationCopyFrame t = new LocationCopyFrame();
        Assert.assertNotNull("exists",t);
        t.setVisible(true);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        LocationCopyFrame f = new LocationCopyFrame();
        Assert.assertNotNull("exists",f);
        f.setVisible(true);
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.copyButton);
        
        // error dialog window show appear
        JemmyUtil.pressDialogButton(f, MessageFormat.format(Bundle
                .getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("ButtonCopy")}), Bundle.getMessage("ButtonOK"));
        
        // enter a name for the new track
        f.loctionNameTextField.setText("Test location name");
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.copyButton);
        
        // error dialog window show appear
        JemmyUtil.pressDialogButton(f, MessageFormat.format(Bundle
                .getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("ButtonCopy")}), Bundle.getMessage("ButtonOK"));
        
        JemmyUtil.waitFor(f);
        
        // select a location to copy
        f.locationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(f.moveRollingStockCheckBox);
        JemmyUtil.enterClickAndLeave(f.deleteTrackCheckBox);
        
        JemmyUtil.enterClickAndLeave(f.copyButton);
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("Test location name");
        Assert.assertNotNull("exists", loc);

        JUnitUtil.dispose(f);

    }

    // private final static Logger log = LoggerFactory.getLogger(LocationCopyFrameTest.class);

}
