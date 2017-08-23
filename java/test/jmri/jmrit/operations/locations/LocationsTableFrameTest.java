package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class LocationsTableFrameTest extends OperationsSwingTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    @Test
    public void testCtorFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationsTableFrame f = new LocationsTableFrame();
        Assert.assertNotNull("exists", f);
        // close windows
        JFrameOperator jfof = new JFrameOperator(f);
        jfof.close();
    }

    @Test
    public void testTableCreationFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationsTableFrame f = new LocationsTableFrame();

        // should be 5 rows
        Assert.assertEquals("number of rows", 5, f.locationsModel.getRowCount());
        // default is sort by name
        Assert.assertEquals("1st loc", "Test Loc A", f.locationsModel.getValueAt(0, LocationsTableModel.NAMECOLUMN));
        Assert.assertEquals("2nd loc", "Test Loc B", f.locationsModel.getValueAt(1, LocationsTableModel.NAMECOLUMN));
        Assert.assertEquals("3rd loc", "Test Loc C", f.locationsModel.getValueAt(2, LocationsTableModel.NAMECOLUMN));
        Assert.assertEquals("4th loc", "Test Loc D", f.locationsModel.getValueAt(3, LocationsTableModel.NAMECOLUMN));
        Assert.assertEquals("5th loc", "Test Loc E", f.locationsModel.getValueAt(4, LocationsTableModel.NAMECOLUMN));

        // check location lengths
        Assert.assertEquals("1st loc length", 1005, f.locationsModel.getValueAt(0, LocationsTableModel.LENGTHCOLUMN));
        Assert.assertEquals("2nd loc length", 1004, f.locationsModel.getValueAt(1, LocationsTableModel.LENGTHCOLUMN));
        Assert.assertEquals("3rd loc length", 1003, f.locationsModel.getValueAt(2, LocationsTableModel.LENGTHCOLUMN));
        Assert.assertEquals("4th loc length", 1002, f.locationsModel.getValueAt(3, LocationsTableModel.LENGTHCOLUMN));
        Assert.assertEquals("5th loc length", 1001, f.locationsModel.getValueAt(4, LocationsTableModel.LENGTHCOLUMN));

        // close windows
        JFrameOperator jfof = new JFrameOperator(f);
        jfof.close();
    }

    @Test
    public void testLocationsEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationsTableFrame f = new LocationsTableFrame();

        // create edit location frame
        f.locationsModel.setValueAt(null, 2, LocationsTableModel.EDITCOLUMN);

        // confirm location edit frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame("Edit Location") != null;
        }, "lef not null");
        JmriJFrame lef = JmriJFrame.getFrame("Edit Location");
        Assert.assertNotNull(lef);

        // close windows
        JFrameOperator jfolef = new JFrameOperator(lef);
        jfolef.close();
        JFrameOperator jfof = new JFrameOperator(f);
        jfof.close();

        Assert.assertNull(JmriJFrame.getFrame("Edit Location"));

    }

    @Test
    public void testLocationsAddFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationsTableFrame f = new LocationsTableFrame();

        // create edit location frame
        f.locationsModel.setValueAt(null, 2, LocationsTableModel.EDITCOLUMN);

        // create add location frame by clicking add button
        f.addButton.doClick();
        // the following fails on 13" laptops
        //enterClickAndLeave(f.addButton);

        // confirm location add frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame("Add Location") != null;
        }, "lef not null");
        JmriJFrame lef = JmriJFrame.getFrame("Add Location");
        Assert.assertNotNull(lef);

        // close windows
        JFrameOperator jfolef = new JFrameOperator(lef);
        jfolef.close();
        JFrameOperator jfof = new JFrameOperator(f);
        jfof.close();

        Assert.assertNull(JmriJFrame.getFrame("Add Location"));
    }

    private void loadLocations() {
        // create 5 locations
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l1 = lManager.newLocation("Test Loc E");
        l1.setLength(1001);
        Location l2 = lManager.newLocation("Test Loc D");
        l2.setLength(1002);
        Location l3 = lManager.newLocation("Test Loc C");
        l3.setLength(1003);
        Location l4 = lManager.newLocation("Test Loc B");
        l4.setLength(1004);
        Location l5 = lManager.newLocation("Test Loc A");
        l5.setLength(1005);

    }

    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        loadLocations();
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
