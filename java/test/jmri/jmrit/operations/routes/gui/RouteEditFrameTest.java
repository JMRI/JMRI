package jmri.jmrit.operations.routes.gui;

import java.awt.Dimension;
import java.util.List;

import javax.swing.JComboBox;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class RouteEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        RouteEditFrame t = new RouteEditFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testRouteNameTooLong() {

        // The default route name is too long
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveRouteButton);
        JemmyUtil.pressDialogButton(f, "Can not save Route!", Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testRouteNameMissing() {

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(null);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addRouteButton);
        JemmyUtil.pressDialogButton(f, "Can not add Route!", Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        JUnitUtil.dispose(f);
    }

    /**
     * Checks to see if route is assigned to train
     */
    @Test
    public void testNewRoute() {

        TrainManager tManager = InstanceManager.getDefault(TrainManager.class);
        Train train = tManager.newTrain("Test Train");

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(null, train);

        // Add some locations to the route
        JUnitOperationsUtil.loadFiveLocations();

        // confirm title is "Add Route"
        Assert.assertEquals("Title", Bundle.getMessage("TitleRouteAdd"), f.getTitle());
        f.routeNameTextField.setText("New Test Route");
        JemmyUtil.enterClickAndLeave(f.addRouteButton);

        Assert.assertNotNull(train.getRoute());
        Assert.assertEquals("Route Name", "New Test Route", train.getRoute().getName());

        Assert.assertFalse("Add Route Button", f.addRouteButton.isEnabled());

        JUnitUtil.dispose(f);
    }

    /*
     * Test that info message appears when adding location and none selected
     */
    @Test
    public void testRouteEditFrameNoSelection() {

        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Add Route Frame");
        f.initComponents(null);

        f.routeNameTextField.setText("New Test Route");
        f.commentTextField.setText("New Text Route Comment");
        JemmyUtil.enterClickAndLeave(f.addRouteButton);

        JUnitOperationsUtil.loadFiveLocations();

        // no location selected
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addLocationButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("SelectLocation"), Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testRouteEditFrame() {

        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Add Route Frame");
        f.initComponents(null);

        f.routeNameTextField.setText("New Test Route");
        f.commentTextField.setText("New Text Route Comment");
        JemmyUtil.enterClickAndLeave(f.addRouteButton);

        JUnitOperationsUtil.loadFiveRoutes();

        RouteManager rManager = InstanceManager.getDefault(RouteManager.class);
        Assert.assertEquals("should be 6 routes", 6, rManager.getRoutesByNameList().size());
        Route newRoute = rManager.getRouteByName("New Test Route");
        Assert.assertNotNull(newRoute);
        Assert.assertEquals("route comment", "New Text Route Comment", newRoute.getComment());

        // Add some locations to the route
        JUnitOperationsUtil.loadFiveLocations();
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc B"));
        JemmyUtil.enterClickAndLeave(f.addLocationButton);
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc D"));
        JemmyUtil.enterClickAndLeave(f.addLocationButton);
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc A"));
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        // put the next two locations at the start of the route
        JemmyUtil.enterClickAndLeave(f.addLocAtTop);
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc C"));
        JemmyUtil.enterClickAndLeave(f.addLocationButton);
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc E"));
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        // confirm that the route sequence is correct
        List<RouteLocation> routeLocations = newRoute.getLocationsBySequenceList();
        Assert.assertEquals("1st location", "Test Loc E", routeLocations.get(0).getName());
        Assert.assertEquals("2nd location", "Test Loc C", routeLocations.get(1).getName());
        Assert.assertEquals("3rd location", "Test Loc B", routeLocations.get(2).getName());
        Assert.assertEquals("4th location", "Test Loc D", routeLocations.get(3).getName());
        Assert.assertEquals("5th location", "Test Loc A", routeLocations.get(4).getName());

        // put the next location in the middle
        JemmyUtil.enterClickAndLeave(f.addLocAtMiddle);
        f.locationBox.setSelectedItem(lManager.getLocationByName("Test Loc A"));
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        // confirm that the route sequence is correct
        routeLocations = newRoute.getLocationsBySequenceList();
        Assert.assertEquals("1st location", "Test Loc E", routeLocations.get(0).getName());
        Assert.assertEquals("2nd location", "Test Loc C", routeLocations.get(1).getName());
        Assert.assertEquals("3rd location", "Test Loc A", routeLocations.get(2).getName());
        Assert.assertEquals("4th location", "Test Loc B", routeLocations.get(3).getName());
        Assert.assertEquals("5th location", "Test Loc D", routeLocations.get(4).getName());
        Assert.assertEquals("6th location", "Test Loc A", routeLocations.get(5).getName());

        f.routeNameTextField.setText("Newer Test Route");
        JemmyUtil.enterClickAndLeave(f.saveRouteButton);

        Assert.assertEquals("changed route name", "Newer Test Route", newRoute.getName());

        // test delete button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteRouteButton);
        // click "Yes" in the confirm popup
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("DeleteRoute?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        Assert.assertEquals("should be 5 routes", 5, rManager.getRoutesByNameList().size());
        JUnitUtil.dispose(f);
    }

    @Test
    public void testRouteLocationComment() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        RouteLocation rl = route.getDepartsRouteLocation();

        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Table Buttons");
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);

        // test add comment to route location
        JemmyUtil.clickOnCellThreadSafe(tbl, 0, Bundle.getMessage("Comment"));

        // find comment window by name
        JDialogOperator jdo = new JDialogOperator(Bundle.getMessage("Comment") + " " + rl.getName());
        JTextAreaOperator jtao = new JTextAreaOperator(jdo);
        jtao.setText("Happy Days");
        JButtonOperator jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonOK"));
        jbo.doClick();
        Assert.assertEquals("confirm comment", "Happy Days", rl.getComment());

        // test cancel comment
        JemmyUtil.clickOnCellThreadSafe(tbl, 0, Bundle.getMessage("Comment"));

        // find comment window by name
        jdo = new JDialogOperator(Bundle.getMessage("Comment") + " " + rl.getName());
        jtao = new JTextAreaOperator(jdo);
        jtao.setText("So Sad!");
        jbo = new JButtonOperator(jdo, Bundle.getMessage("ButtonCancel"));
        jbo.doClick();
        Assert.assertEquals("confirm comment", "Happy Days", rl.getComment());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testRouteLocationDelete() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("Confirm departure name", "Acton", rl.getName());

        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Table Buttons");
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);

        // test delete route location
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("ButtonDelete")));

        rl = route.getDepartsRouteLocation();
        Assert.assertEquals("Confirm departure name", "Boston", rl.getName());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testButtonDown() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("Confirm departure name", "Acton", rl.getName());

        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Table Buttons");
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Down")));

        rl = route.getDepartsRouteLocation();
        Assert.assertEquals("Confirm departure name", "Boston", rl.getName());

        rl = route.getRouteLocationBySequenceNumber(2);
        Assert.assertEquals("Confirm departure name", "Acton", rl.getName());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testButtonUp() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("Confirm departure name", "Acton", rl.getName());

        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Table Buttons");
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        // findColumn finds the first column with the letters "up"
        //        tbl.clickOnCell(0, tbl.findColumn(Bundle.getMessage("Up")));
        tbl.clickOnCell(0, 15);

        rl = route.getDepartsRouteLocation();
        Assert.assertEquals("Confirm departure name", "Boston", rl.getName());

        rl = route.getRouteLocationBySequenceNumber(2);
        Assert.assertEquals("Confirm departure name", "Chelmsford", rl.getName());

        rl = route.getTerminatesRouteLocation();
        Assert.assertEquals("Confirm departure name", "Acton", rl.getName());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testWait() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        route.setName("5 Locations");
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("Confirm departure name", "Acton", rl.getName());

        RouteEditFrame f = new RouteEditFrame();
        f.setTitle("Test Table Buttons");
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        // confirm default wait value
        Assert.assertEquals("Wait", 0, rl.getWait());
        Assert.assertEquals("Time", 4, Setup.getTravelTime());

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);

        Assert.assertEquals("Travel Time", 4, (int) tbl.getValueAt(0, tbl.findColumn(Bundle.getMessage("Travel"))));
        tbl.setValueAt(20, 0, tbl.findColumn(Bundle.getMessage("Travel")));
        JemmyUtil.enterClickAndLeave(f.saveRouteButton);
        // wait = travel - time
        Assert.assertEquals("New Wait", 16, rl.getWait());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testDeputureTime() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        route.setName("5 Locations");
        RouteLocation rl = route.getDepartsRouteLocation();
        Assert.assertEquals("Confirm departure name", "Acton", rl.getName());
        rl.setDepartureTime("06", "05");

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        JComboBox<String> box = f.routeModel.getTimeComboBox();
        box.setSelectedItem("20:45");

        tbl.setValueAt(box, 0, tbl.findColumn(Bundle.getMessage("Time")));
        JemmyUtil.enterClickAndLeave(f.saveRouteButton);
        Assert.assertEquals("New departure time", "20:45", rl.getDepartureTime());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testMaxMoves() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        route.setName("5 Locations");
        RouteLocation rl = route.getDepartsRouteLocation();

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        // confirm default value
        Assert.assertEquals("Max Moves", 5, rl.getMaxCarMoves());

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.setValueAt(50, 0, tbl.findColumn(Bundle.getMessage("Moves")));
        JemmyUtil.enterClickAndLeave(f.saveRouteButton);
        Assert.assertEquals("New Max", 50, rl.getMaxCarMoves());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetXY() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        route.setName("5 Locations");
        RouteLocation rl = route.getDepartsRouteLocation();

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        // confirm default value
        Assert.assertEquals("X Corrdinate", 0, rl.getTrainIconX());
        Assert.assertEquals("Y Corrdinate", 0, rl.getTrainIconY());

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        // findColumn returns the first column with the letter "x".
        //        tbl.setValueAt(23, 0, tbl.findColumn(Bundle.getMessage("X")));      
        tbl.setValueAt(23, 0, 12);
        tbl.setValueAt(57, 0, tbl.findColumn(Bundle.getMessage("Y")));
        JemmyUtil.enterClickAndLeave(f.saveRouteButton);
        Assert.assertEquals("New X Corrdinate", 23, rl.getTrainIconX());
        Assert.assertEquals("New Y Corrdinate", 57, rl.getTrainIconY());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testGrade() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        route.setName("5 Locations");
        RouteLocation rl = route.getDepartsRouteLocation();

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        // confirm default value
        Assert.assertEquals("Grade", 0, (int) rl.getGrade());

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        Double grade = 3.3;
        tbl.setValueAt(grade, 0, tbl.findColumn(Bundle.getMessage("Grade")));
        JemmyUtil.enterClickAndLeave(f.saveRouteButton);
        Assert.assertEquals("New Grade", grade, (Double) rl.getGrade());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testMaxTrainLength() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        route.setName("5 Locations");
        RouteLocation rl = route.getDepartsRouteLocation();

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        // confirm default value
        Assert.assertEquals("Max Length", 1000, rl.getMaxTrainLength());

        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.setValueAt(950, 0, tbl.findColumn(Bundle.getMessage("MaxLength")));
        JemmyUtil.enterClickAndLeave(f.saveRouteButton);
        Assert.assertEquals("New Max Length", 950, rl.getMaxTrainLength());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testMinTrainLength() {

        // create a route
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        route.setName("5 Locations");
        RouteLocation rl = route.getDepartsRouteLocation();

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);

        // need to show entire table
        f.setSize(new Dimension(1200, Control.panelHeight400));

        // confirm default value
        Assert.assertEquals("Max Length", 1000, rl.getMaxTrainLength());

        Thread t = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("WarningTooShort"),
                Bundle.getMessage("ButtonCancel"));
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.setValueAt(499, 0, tbl.findColumn(Bundle.getMessage("MaxLength")));
        JUnitUtil.waitFor(() -> !t.isAlive(), "TooShort Cancel dialog");
        Assert.assertEquals("Old Max Length", 1000, rl.getMaxTrainLength());

        Thread tt = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("WarningTooShort"),
                Bundle.getMessage("ButtonOK"));
        tbl.setValueAt(499, 0, tbl.findColumn(Bundle.getMessage("MaxLength")));
        JUnitUtil.waitFor(() -> !tt.isAlive(), "TooShort OK dialog");
        Assert.assertEquals("New Max Length", 499, rl.getMaxTrainLength());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testRouteEditFrameRead() {

        JUnitOperationsUtil.loadFiveRoutes();
        RouteManager rManager = InstanceManager.getDefault(RouteManager.class);
        Route route = rManager.getRouteByName("Test Route C");

        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);

        Assert.assertEquals("route name", "Test Route C", f.routeNameTextField.getText());
        Assert.assertEquals("route comment", "Comment test route C", f.commentTextField.getText());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCloseWindowOnSave() {

        RouteManager rManager = InstanceManager.getDefault(RouteManager.class);
        Route route = rManager.newRoute("Test Route");
        RouteEditFrame f = new RouteEditFrame();
        f.initComponents(route);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }
}
