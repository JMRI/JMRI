package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class LocationsTableFrameTest extends OperationsTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    @Test
    public void testCtorFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationsTableFrame f = new LocationsTableFrame();
        Assert.assertNotNull("exists", f);
        // close windows
        JFrameOperator jfof = new JFrameOperator(f);
        jfof.dispose();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testTableCreationFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadFiveLocations();
        LocationsTableFrame f = new LocationsTableFrame();

        // should be 5 rows
        Assert.assertEquals("number of rows", 5, f.locationsModel.getRowCount());
        // default is sort by name
        Assert.assertEquals("1st loc", "Test Loc A", f.locationsModel.getValueAt(0, LocationsTableModel.NAME_COLUMN));
        Assert.assertEquals("2nd loc", "Test Loc B", f.locationsModel.getValueAt(1, LocationsTableModel.NAME_COLUMN));
        Assert.assertEquals("3rd loc", "Test Loc C", f.locationsModel.getValueAt(2, LocationsTableModel.NAME_COLUMN));
        Assert.assertEquals("4th loc", "Test Loc D", f.locationsModel.getValueAt(3, LocationsTableModel.NAME_COLUMN));
        Assert.assertEquals("5th loc", "Test Loc E", f.locationsModel.getValueAt(4, LocationsTableModel.NAME_COLUMN));

        // check location lengths
        Assert.assertEquals("1st loc length", 1005, f.locationsModel.getValueAt(0, LocationsTableModel.LENGTH_COLUMN));
        Assert.assertEquals("2nd loc length", 1004, f.locationsModel.getValueAt(1, LocationsTableModel.LENGTH_COLUMN));
        Assert.assertEquals("3rd loc length", 1003, f.locationsModel.getValueAt(2, LocationsTableModel.LENGTH_COLUMN));
        Assert.assertEquals("4th loc length", 1002, f.locationsModel.getValueAt(3, LocationsTableModel.LENGTH_COLUMN));
        Assert.assertEquals("5th loc length", 1001, f.locationsModel.getValueAt(4, LocationsTableModel.LENGTH_COLUMN));

        // close windows
        JFrameOperator jfof = new JFrameOperator(f);
        jfof.dispose();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testLocationsEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadFiveLocations();
        LocationsTableFrame f = new LocationsTableFrame();

        // create edit location frame
        f.locationsModel.setValueAt(null, 2, LocationsTableModel.EDIT_COLUMN);

        // confirm location edit frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("TitleLocationEdit")) != null;
        }, "lef not null");
        JmriJFrame lef = JmriJFrame.getFrame(Bundle.getMessage("TitleLocationEdit"));
        Assert.assertNotNull(lef);

        // close windows
        JFrameOperator jfolef = new JFrameOperator(lef);
        jfolef.dispose();
        JFrameOperator jfof = new JFrameOperator(f);
        jfof.dispose();

        Assert.assertNull(JmriJFrame.getFrame(Bundle.getMessage("TitleLocationEdit")));
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testLocationsAddFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationsTableFrame f = new LocationsTableFrame();

        // create edit location frame
        f.locationsModel.setValueAt(null, 2, LocationsTableModel.EDIT_COLUMN);

        // create add location frame by clicking add button
        f.addButton.doClick();
        // the following fails on 13" laptops
        //JemmyUtil.enterClickAndLeave(f.addButton);

        // confirm location add frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("AddLocation")) != null;
        }, "lef not null");
        JmriJFrame lef = JmriJFrame.getFrame(Bundle.getMessage("AddLocation"));
        Assert.assertNotNull(lef);

        // close windows
        JFrameOperator jfolef = new JFrameOperator(lef);
        jfolef.dispose();
        JFrameOperator jfof = new JFrameOperator(f);
        jfof.dispose();

        Assert.assertNull(JmriJFrame.getFrame(Bundle.getMessage("AddLocation")));
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
}
