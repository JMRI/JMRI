package jmri.jmrit.operations.routes;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class RoutesTableFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        RoutesTableFrame t = new RoutesTableFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testRoutesTableFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadFiveRoutes();

        RoutesTableFrame f = new RoutesTableFrame();
        // entire table must be showing
        f.setSize(new Dimension(Control.panelWidth1025, Control.panelHeight300));

        // should be 5 rows
        Assert.assertEquals("number of rows", 5, f.routesModel.getRowCount());
        // default is sort by name
        Assert.assertEquals("1st route", "Test Route A", f.routesModel.getValueAt(0, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("2nd route", "Test Route B", f.routesModel.getValueAt(1, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("3rd route", "Test Route C", f.routesModel.getValueAt(2, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("4th route", "Test Route D", f.routesModel.getValueAt(3, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("5th route", "Test Route E", f.routesModel.getValueAt(4, RoutesTableModel.NAME_COLUMN));

        // test sort by id
        f.sortById.doClick();
        Assert.assertEquals("1st route", "Test Route E", f.routesModel.getValueAt(0, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("2nd route", "Test Route D", f.routesModel.getValueAt(1, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("3rd route", "Test Route C", f.routesModel.getValueAt(2, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("4th route", "Test Route B", f.routesModel.getValueAt(3, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("5th route", "Test Route A", f.routesModel.getValueAt(4, RoutesTableModel.NAME_COLUMN));
        
        // test sort by name
        f.sortByName.doClick();
        Assert.assertEquals("1st route", "Test Route A", f.routesModel.getValueAt(0, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("2nd route", "Test Route B", f.routesModel.getValueAt(1, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("3rd route", "Test Route C", f.routesModel.getValueAt(2, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("4th route", "Test Route D", f.routesModel.getValueAt(3, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("5th route", "Test Route E", f.routesModel.getValueAt(4, RoutesTableModel.NAME_COLUMN));
        
        // create add route frame
        f.addButton.doClick();
        // the following fails on a 13" laptop
        //JemmyUtil.enterClickAndLeave(f.addButton);
        // confirm panel creation
        JmriJFrame ref = JmriJFrame.getFrame(Bundle.getMessage("TitleRouteAdd"));
        Assert.assertNotNull("route edit frame", ref);

        // create edit route frame
        f.routesModel.setValueAt(null, 2, RoutesTableModel.EDIT_COLUMN);

        JUnitUtil.dispose(ref);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }


}
