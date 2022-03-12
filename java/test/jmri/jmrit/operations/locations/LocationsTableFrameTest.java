package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.divisions.Division;
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
        JFrameOperator jfof = new JFrameOperator(f);

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

        // check ids
        Assert.assertEquals("1st loc id", 5, f.locationsModel.getValueAt(0, LocationsTableModel.ID_COLUMN));
        Assert.assertEquals("2nd loc id", 4, f.locationsModel.getValueAt(1, LocationsTableModel.ID_COLUMN));
        Assert.assertEquals("3rd loc id", 3, f.locationsModel.getValueAt(2, LocationsTableModel.ID_COLUMN));
        Assert.assertEquals("4th loc id", 2, f.locationsModel.getValueAt(3, LocationsTableModel.ID_COLUMN));
        Assert.assertEquals("5th loc id", 1, f.locationsModel.getValueAt(4, LocationsTableModel.ID_COLUMN));

        // change sort to id
        JRadioButtonOperator jrbo = new JRadioButtonOperator(jfof, Bundle.getMessage("Id"));
        jrbo.doClick();
        
        // check ids
        Assert.assertEquals("1st loc id", 1, f.locationsModel.getValueAt(0, LocationsTableModel.ID_COLUMN));
        Assert.assertEquals("2nd loc id", 2, f.locationsModel.getValueAt(1, LocationsTableModel.ID_COLUMN));
        Assert.assertEquals("3rd loc id", 3, f.locationsModel.getValueAt(2, LocationsTableModel.ID_COLUMN));
        Assert.assertEquals("4th loc id", 4, f.locationsModel.getValueAt(3, LocationsTableModel.ID_COLUMN));
        Assert.assertEquals("5th loc id", 5, f.locationsModel.getValueAt(4, LocationsTableModel.ID_COLUMN));
     
        // change sort to name
        JRadioButtonOperator jrbon = new JRadioButtonOperator(jfof, Bundle.getMessage("Name"));
        jrbon.doClick();
        Assert.assertEquals("1st loc", "Test Loc A", f.locationsModel.getValueAt(0, LocationsTableModel.NAME_COLUMN));
        
        // add division column
        JTableOperator tbl = new JTableOperator(jfof);
        Assert.assertEquals("column not found", -1, tbl.findColumn(Bundle.getMessage("Division")));
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location loc1 = lManager.getLocationById("1");
        loc1.setDivision(new Division("id1", "Division 1"));
        Assert.assertEquals("column found", 11, tbl.findColumn(Bundle.getMessage("Division")));
        
        lManager.deregister(loc1);
        Assert.assertEquals("number of rows", 4, f.locationsModel.getRowCount());
        // TODO not sure if this is correct, table still shows the division column after deleting location
        Assert.assertEquals("column not found", 11, tbl.findColumn(Bundle.getMessage("Division")));
        
        // close windows
        jfof.dispose();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testLocationsEditButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadFiveLocations();
        LocationsTableFrame f = new LocationsTableFrame();

        // create edit location frame
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(2, tbl.findColumn(Bundle.getMessage("ButtonEdit")));

        // confirm location edit frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("TitleLocationEdit")) != null;
        }, "lef not null");
        JmriJFrame lef = JmriJFrame.getFrame(Bundle.getMessage("TitleLocationEdit"));
        Assert.assertNotNull(lef);
        
        // edit location again for test coverage
        tbl.clickOnCell(2, tbl.findColumn(Bundle.getMessage("ButtonEdit")));

        // close windows
        JFrameOperator jfolef = new JFrameOperator(lef);
        jfolef.dispose();
        jfo.dispose();

        Assert.assertNull(JmriJFrame.getFrame(Bundle.getMessage("TitleLocationEdit")));
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testLocationsYardmasterButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.loadFiveLocations();
        LocationsTableFrame f = new LocationsTableFrame();

        // create edit location frame
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(4, tbl.findColumn(Bundle.getMessage("Action")));
        
        String yardMasterFrameName = Bundle.getMessage("Yardmaster") + " (Test Loc E)";

        // confirm Yardmaster frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(yardMasterFrameName) != null;
        }, "yef not null");
        JmriJFrame yef = JmriJFrame.getFrame(yardMasterFrameName);
        Assert.assertNotNull(yef);

        // close windows
        JFrameOperator jfolef = new JFrameOperator(yef);
        jfolef.dispose();
        jfo.dispose();

        Assert.assertNull(JmriJFrame.getFrame(yardMasterFrameName));
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testLocationsAddFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationsTableFrame f = new LocationsTableFrame();

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
