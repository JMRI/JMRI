package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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
        LocationCopyFrame t = new LocationCopyFrame();
        Assert.assertNotNull("exists",t);
        t.setVisible(true);
        
        JemmyUtil.enterClickAndLeave(t.copyButton);
        
        // error dialog window show appear
        JemmyUtil.pressDialogButton(t, MessageFormat.format(Bundle
                .getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("ButtonCopy")}), "OK");
        
        // enter a name for the new track
        t.loctionNameTextField.setText("Test location name");
        
        JemmyUtil.enterClickAndLeave(t.copyButton);
        
        // error dialog window show appear
        JemmyUtil.pressDialogButton(t, MessageFormat.format(Bundle
                .getMessage("CanNotLocation"), new Object[]{Bundle.getMessage("ButtonCopy")}), "OK");
        
        // select a location to copy
        t.locationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(t.moveRollingStockCheckBox);
        JemmyUtil.enterClickAndLeave(t.deleteTrackCheckBox);
        
        JemmyUtil.enterClickAndLeave(t.copyButton);
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("Test location name");
        Assert.assertNotNull("exists", loc);

        JUnitUtil.dispose(t);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();

        JUnitOperationsUtil.initOperationsData();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LocationCopyFrameTest.class);

}
