//OperationsRoutesGuiTest.java
package jmri.jmrit.operations.routes;

import java.util.List;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.util.JmriJFrame;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations Routes GUI class
 *
 * @author	Dan Boudreau Copyright (C) 2009
 */
public class OperationsRoutesGuiTest extends OperationsSwingTestCase {

    public void testRoutesTableFrame() {
        loadRoutes();

        RoutesTableFrame f = new RoutesTableFrame();

        // should be 5 rows
        Assert.assertEquals("number of rows", 5, f.routesModel.getRowCount());
        // default is sort by name
        Assert.assertEquals("1st route", "Test Route A", f.routesModel.getValueAt(0, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("2nd route", "Test Route B", f.routesModel.getValueAt(1, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("3rd route", "Test Route C", f.routesModel.getValueAt(2, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("4th route", "Test Route D", f.routesModel.getValueAt(3, RoutesTableModel.NAME_COLUMN));
        Assert.assertEquals("5th route", "Test Route E", f.routesModel.getValueAt(4, RoutesTableModel.NAME_COLUMN));

        // create add route frame
        //f.addButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
        // confirm panel creation
        JmriJFrame ref = JmriJFrame.getFrame("Add Route");
        Assert.assertNotNull("route edit frame", ref);

        // create edit route frame
        f.routesModel.setValueAt(null, 2, RoutesTableModel.EDIT_COLUMN);

        ref.dispose();
        f.dispose();
    }

    public void testRouteEditFrame() {
        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Add Route Frame");
        f.initComponents(null);

        f.routeNameTextField.setText("New Test Route");
        f.commentTextField.setText("New Text Route Comment");
        //f.addRouteButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addRouteButton));
        
        loadRoutes();

        RouteManager rManager = RouteManager.instance();
        Assert.assertEquals("should be 6 routes", 6, rManager.getRoutesByNameList().size());
        Route newRoute = rManager.getRouteByName("New Test Route");
        Assert.assertNotNull(newRoute);
        Assert.assertEquals("route comment", "New Text Route Comment", newRoute.getComment());

        // Add some locations to the route
        loadLocations();
        LocationManager lManager = LocationManager.instance();
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc B"));
        //f.addLocationButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addLocationButton));
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc D"));
        //f.addLocationButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addLocationButton));
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc A"));
        //f.addLocationButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addLocationButton));

        // put the next two locations at the start of the route
        //f.addLocAtTop.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addLocAtTop));
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc C"));
        //f.addLocationButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addLocationButton));
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc E"));
        //f.addLocationButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addLocationButton));

        // confirm that the route sequence is correct
        List<RouteLocation> routeLocations = newRoute.getLocationsBySequenceList();
        Assert.assertEquals("1st location", "Test Loc E", routeLocations.get(0).getName());
        Assert.assertEquals("2nd location", "Test Loc C", routeLocations.get(1).getName());
        Assert.assertEquals("3rd location", "Test Loc B", routeLocations.get(2).getName());
        Assert.assertEquals("4th location", "Test Loc D", routeLocations.get(3).getName());
        Assert.assertEquals("5th location", "Test Loc A", routeLocations.get(4).getName());

        f.routeNameTextField.setText("Newer Test Route");
        //f.saveRouteButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveRouteButton));

        Assert.assertEquals("changed route name", "Newer Test Route", newRoute.getName());

        // test delete button
        //f.deleteRouteButton.doClick();
        getHelper().enterClickAndLeave(new MouseEventData(this, f.deleteRouteButton));
        // click "Yes" in the confirm popup
        pressDialogButton(f, "Yes");

        Assert.assertEquals("should be 5 routes", 5, rManager.getRoutesByNameList().size());

        f.dispose();
    }

    public void testRouteEditFrameRead() {
        loadRoutes();
        RouteManager lManager = RouteManager.instance();
        Route l2 = lManager.getRouteByName("Test Route C");

        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Edit Route Frame");
        f.initComponents(l2);

        Assert.assertEquals("route name", "Test Route C", f.routeNameTextField.getText());
        Assert.assertEquals("route comment", "Comment test route C", f.commentTextField.getText());

        f.dispose();
    }
    
    private void loadLocations() {
        // create 5 locations
        LocationManager lManager = LocationManager.instance();
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
    
    private void loadRoutes() {
        RouteManager rManager = RouteManager.instance();
        Route r1 = rManager.newRoute("Test Route E");
        r1.setComment("Comment test route E");
        Route r2 = rManager.newRoute("Test Route D");
        r2.setComment("Comment test route D");
        Route r3 = rManager.newRoute("Test Route C");
        r3.setComment("Comment test route C");
        Route r4 = rManager.newRoute("Test Route B");
        r4.setComment("Comment test route B");
        Route r5 = rManager.newRoute("Test Route A");
        r5.setComment("Comment test route A");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public OperationsRoutesGuiTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OperationsRoutesGuiTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OperationsRoutesGuiTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
