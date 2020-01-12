package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;

import org.junit.*;


import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainEditBuildOptionsFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainEditBuildOptionsFrame t = new TrainEditBuildOptionsFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testTrainEditBuildOptionsFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // test build options
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train t = tmanager.newTrain("Test Train New Name");

        // Add a route to this train
        Route route = InstanceManager.getDefault(RouteManager.class).newRoute("Test Train Route");
        route.addLocation(InstanceManager.getDefault(LocationManager.class).newLocation("Test Train Location A"));
        route.addLocation(InstanceManager.getDefault(LocationManager.class).newLocation("Test Train Location B"));
        route.addLocation(InstanceManager.getDefault(LocationManager.class).newLocation("Test Train Location C"));
        t.setRoute(route);

        TrainEditFrame trainEditFrame = new TrainEditFrame(t);
        trainEditFrame.setLocation(0, 0); // entire panel must be visible for tests to work properly
        trainEditFrame.setTitle("Test Build Options Train Frame");
        
        // Normal build option is only enabled when building aggressive
        Setup.setBuildAggressive(true);

        TrainEditBuildOptionsFrame f = new TrainEditBuildOptionsFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents(trainEditFrame);
        f.setTitle("Test Train Build Options");

        // confirm defaults
        Assert.assertEquals("Build normal", false, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", false, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        // test options
        JemmyUtil.enterClickAndLeave(f.buildNormalCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", false, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        JemmyUtil.enterClickAndLeave(f.sendToTerminalCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        JemmyUtil.enterClickAndLeave(f.returnStagingCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
        // the return to staging checkbox should be disabled
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", true, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        JemmyUtil.enterClickAndLeave(f.allowLocalMovesCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", false, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", true, t.isAllowThroughCarsEnabled());

        JemmyUtil.enterClickAndLeave(f.allowThroughCarsCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Build normal", true, t.isBuildTrainNormalEnabled());
        Assert.assertEquals("send to terminal", true, t.isSendCarsToTerminalEnabled());
        Assert.assertEquals("return to staging", false, t.isAllowReturnToStagingEnabled());
        Assert.assertEquals("allow local moves", false, t.isAllowLocalMovesEnabled());
        Assert.assertEquals("allow through cars", false, t.isAllowThroughCarsEnabled());

        // test car owner options
        JemmyUtil.enterClickAndLeave(f.ownerNameExclude);

        Assert.assertEquals("train car owner exclude", Train.EXCLUDE_OWNERS, t.getOwnerOption());
        JemmyUtil.enterClickAndLeave(f.ownerNameInclude);

        Assert.assertEquals("train car owner include", Train.INCLUDE_OWNERS, t.getOwnerOption());
        JemmyUtil.enterClickAndLeave(f.ownerNameAll);

        Assert.assertEquals("train car owner all", Train.ALL_OWNERS, t.getOwnerOption());

        // test car date options
        JemmyUtil.enterClickAndLeave(f.builtDateAfter);

        f.builtAfterTextField.setText("1956");
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("train car built after", "1956", t.getBuiltStartYear());

        JemmyUtil.enterClickAndLeave(f.builtDateBefore);

        f.builtBeforeTextField.setText("2010");
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("train car built before", "2010", t.getBuiltEndYear());

        JemmyUtil.enterClickAndLeave(f.builtDateRange);

        f.builtAfterTextField.setText("1888");
        f.builtBeforeTextField.setText("2000");
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("train car built after range", "1888", t.getBuiltStartYear());
        Assert.assertEquals("train car built before range", "2000", t.getBuiltEndYear());

        JemmyUtil.enterClickAndLeave(f.builtDateAll);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("train car built after all", "", t.getBuiltStartYear());
        Assert.assertEquals("train car built before all", "", t.getBuiltEndYear());

        // test optional loco and caboose changes
        JemmyUtil.enterClickAndLeave(f.change1Engine);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotSave"), Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("loco 1 change", Train.CHANGE_ENGINES, t.getSecondLegOptions());
        Assert.assertEquals("loco 1 departure name", "", t.getSecondLegStartLocationName());

        f.routePickup1Box.setSelectedIndex(1); // should be "Test Train Location A"
        f.numEngines1Box.setSelectedIndex(3); // should be 3 locos
        f.modelEngine1Box.setSelectedItem("FT");
        f.roadEngine1Box.setSelectedItem("UP");

        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("loco 1 change", Train.CHANGE_ENGINES, t.getSecondLegOptions());
        Assert.assertEquals("loco 1 departure name", "Test Train Location A", t
                .getSecondLegStartLocationName());
        Assert.assertEquals("loco 1 number of engines", "3", t.getSecondLegNumberEngines());
        Assert.assertEquals("loco 1 model", "FT", t.getSecondLegEngineModel());
        Assert.assertEquals("loco 1 road", "UP", t.getSecondLegEngineRoad());

        JemmyUtil.enterClickAndLeave(f.modify1Caboose);

        f.routePickup1Box.setSelectedIndex(0);
        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        f.roadCaboose1Box.setSelectedItem(roadNames[2]);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotSave"), Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("caboose 1 change", Train.ADD_CABOOSE, t.getSecondLegOptions());

        f.routePickup1Box.setSelectedIndex(2);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("caboose 1 road", roadNames[2], t.getSecondLegCabooseRoad());

        JemmyUtil.enterClickAndLeave(f.helper1Service);

        f.routePickup1Box.setSelectedIndex(0);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotSave"), Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("helper 1 change", Train.HELPER_ENGINES, t.getSecondLegOptions());

        f.routePickup1Box.setSelectedIndex(2); // Should be "Test Train Location B"
        f.routeDrop1Box.setSelectedIndex(3); // Should be "Test Train Location C"
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Helper 1 start location name", "Test Train Location B", t
                .getSecondLegStartLocationName());
        Assert.assertEquals("Helper 1 end location name", "Test Train Location C", t
                .getSecondLegEndLocationName());

        JemmyUtil.enterClickAndLeave(f.none1);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("none 1", 0, t.getSecondLegOptions());

        // now do the second set of locos and cabooses
        JemmyUtil.enterClickAndLeave(f.change2Engine);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotSave"), Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("loco 2 change", Train.CHANGE_ENGINES, t.getThirdLegOptions());
        Assert.assertEquals("loco 2 departure name", "", t.getThirdLegStartLocationName());

        f.routePickup2Box.setSelectedIndex(1); // should be "Test Train Location A"
        f.numEngines2Box.setSelectedIndex(3); // should be 3 locos
        f.modelEngine2Box.setSelectedItem("FT");
        f.roadEngine2Box.setSelectedItem("UP");

        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("loco 2 change", Train.CHANGE_ENGINES, t.getThirdLegOptions());
        Assert.assertEquals("loco 2 departure name", "Test Train Location A", t
                .getThirdLegStartLocationName());
        Assert.assertEquals("loco 2 number of engines", "3", t.getThirdLegNumberEngines());
        Assert.assertEquals("loco 2 model", "FT", t.getThirdLegEngineModel());
        Assert.assertEquals("loco 2 road", "UP", t.getThirdLegEngineRoad());

        JemmyUtil.enterClickAndLeave(f.modify2Caboose);

        f.routePickup2Box.setSelectedIndex(0);
        f.roadCaboose2Box.setSelectedItem(roadNames[2]);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotSave"), Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("caboose 2 change", Train.ADD_CABOOSE, t.getThirdLegOptions());

        f.routePickup2Box.setSelectedIndex(2);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("caboose 2 road", roadNames[2], t.getThirdLegCabooseRoad());

        JemmyUtil.enterClickAndLeave(f.helper2Service);

        f.routePickup2Box.setSelectedIndex(0);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        // clear dialogue box
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("CanNotSave"), Bundle.getMessage("ButtonOK"));

        Assert.assertEquals("helper 2 change", Train.HELPER_ENGINES, t.getThirdLegOptions());

        f.routePickup2Box.setSelectedIndex(2); // Should be "Test Train Location B"
        f.routeDrop2Box.setSelectedIndex(3); // Should be "Test Train Location C"
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("Helper 2 start location name", "Test Train Location B", t
                .getThirdLegStartLocationName());
        Assert.assertEquals("Helper 2 end location name", "Test Train Location C", t
                .getThirdLegEndLocationName());

        JemmyUtil.enterClickAndLeave(f.none2);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);

        Assert.assertEquals("none 2", 0, t.getThirdLegOptions());

        JUnitUtil.dispose(trainEditFrame);
        JUnitUtil.dispose(f);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
        JUnitOperationsUtil.loadTrains();
     }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainEditBuildOptionsFrameTest.class);

}
