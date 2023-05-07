package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ShowTrainsServingLocationFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        ShowTrainsServingLocationFrame t = new ShowTrainsServingLocationFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testFrameNoLocationOrTrack() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        ShowTrainsServingLocationFrame stslf = new ShowTrainsServingLocationFrame();
        stslf.initComponents(null, null);
        Assert.assertNotNull("exists", stslf);
        JUnitUtil.dispose(stslf);
    }
    
    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Location ni = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");
        ShowTrainsServingLocationFrame stslf = new ShowTrainsServingLocationFrame();
        Track track = ni.getTracksList().get(0);
        stslf.initComponents(ni, track);
        Assert.assertNotNull("exists", stslf);
        JUnitUtil.dispose(stslf);
    }
    
    @Test
    public void testComboBoxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        ShowTrainsServingLocationFrame stslf = new ShowTrainsServingLocationFrame();
        stslf.initComponents(null, null);
        Assert.assertNotNull("exists", stslf);
        
        stslf.locationComboBox.setSelectedIndex(1);
        stslf.trackComboBox.setSelectedIndex(1);
        stslf.trackComboBox.setSelectedIndex(0); // for property change
        stslf.typeComboBox.setSelectedIndex(1);
        
        // TODO confirm ComboBox changes
        
        JemmyUtil.enterClickAndLeave(stslf.showAllTrainsCheckBox);
        
        JUnitUtil.dispose(stslf);
    }
}
