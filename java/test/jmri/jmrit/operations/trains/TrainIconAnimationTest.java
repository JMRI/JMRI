package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainIconAnimationTest extends OperationsTestCase {

    private final int DIRECTION_ALL = Location.EAST + Location.WEST + Location.NORTH + Location.SOUTH;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.jmrit.display.EditorScaffold es = new jmri.jmrit.display.EditorScaffold();
        TrainIcon ti = new TrainIcon(es);
        Location l1 = new Location("1", "North End");
        Route r1 = new Route("1", "Southbound Main Route");
        RouteLocation rl1 = new RouteLocation("1r1", l1);
        r1.register(rl1);
        TrainIconAnimation t = new TrainIconAnimation(ti, rl1, null);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(es);
    }

    // This test tests that the train icon actually follows movement
    // of a path on a panel.
    @Test
    public void testTrainIconAnimation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        EngineTypes et = InstanceManager.getDefault(EngineTypes.class);

        // create and register a panel
        jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor(
                "Train Test Panel");
        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(editor);

        // confirm panel creation
        JmriJFrame f = JmriJFrame.getFrame("Train Test Panel");
        Assert.assertNotNull(f);

        // Place train icons on panel
        Setup.setPanelName("Train Test Panel");
        // Set terminate color to yellow
        Setup.setTrainIconColorTerminate(TrainIcon.YELLOW);
        // add engine number
        Setup.setTrainIconAppendEnabled(true);

        et.addName("Diesel");

        // Set up four engines in two consists
        Consist con1 = emanager.newConsist("C16");
        Consist con2 = emanager.newConsist("C14");

        Engine e1 = new Engine("PC", "5016");
        e1.setModel("GP40");
        e1.setConsist(con1);
        e1.setMoves(123);
        e1.setOwner("AT");
        e1.setBuilt("1990");
        Assert.assertEquals("Engine 1 Length", "59", e1.getLength());
        emanager.register(e1);

        Engine e2 = new Engine("PC", "5019");
        e2.setModel("GP40");
        e2.setConsist(con1);
        e2.setMoves(321);
        e2.setOwner("AT");
        e2.setBuilt("1990");
        Assert.assertEquals("Engine 2 Length", "59", e2.getLength());
        emanager.register(e2);

        Engine e3 = new Engine("PC", "5524");
        e3.setModel("SD45");
        e3.setConsist(con2);
        e3.setOwner("DAB");
        e3.setBuilt("1980");
        Assert.assertEquals("Engine 3 Length", "66", e3.getLength());
        emanager.register(e3);

        Engine e4 = new Engine("PC", "5559");
        e4.setModel("SD45");
        e4.setConsist(con2);
        e4.setOwner("DAB");
        e4.setBuilt("1980");
        Assert.assertEquals("Engine 4 Length", "66", e4.getLength());
        emanager.register(e4);

        // Set up a route of 3 locations: North End Staging (2 tracks),
        // North Industries (1 track), and South End Staging (2 tracks).
        Location l1 = new Location("1", "North End");
        Assert.assertEquals("Location 1 Id", "1", l1.getId());
        Assert.assertEquals("Location 1 Name", "North End", l1.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, l1.getLength());
        l1.setLocationOps(Location.STAGING);
        l1.setTrainDirections(DIRECTION_ALL);
        l1.setSwitchListEnabled(true);
        lmanager.register(l1);

        Track l1s1 = new Track("1s1", "North End 1", Track.STAGING, l1);
        l1s1.setLength(300);
        Assert.assertEquals("Location 1s1 Id", "1s1", l1s1.getId());
        Assert.assertEquals("Location 1s1 Name", "North End 1", l1s1.getName());
        Assert.assertEquals("Location 1s1 LocType", "Staging", l1s1.getTrackType());
        Assert.assertEquals("Location 1s1 Length", 300, l1s1.getLength());
        l1s1.setTrainDirections(DIRECTION_ALL);
        l1s1.setRoadOption(Track.ALL_ROADS);
        l1s1.setDropOption(Track.ANY);
        l1s1.setPickupOption(Track.ANY);

        Track l1s2 = new Track("1s2", "North End 2", Track.STAGING, l1);
        l1s2.setLength(400);
        Assert.assertEquals("Location 1s2 Id", "1s2", l1s2.getId());
        Assert.assertEquals("Location 1s2 Name", "North End 2", l1s2.getName());
        Assert.assertEquals("Location 1s2 LocType", "Staging", l1s2.getTrackType());
        Assert.assertEquals("Location 1s2 Length", 400, l1s2.getLength());
        l1s2.setTrainDirections(DIRECTION_ALL);
        l1s2.setRoadOption(Track.ALL_ROADS);
        l1s2.setDropOption(Track.ANY);
        l1s2.setPickupOption(Track.ANY);

        l1.addTrack("North End 1", Track.STAGING);
        l1.addTrack("North End 2", Track.STAGING);
        List<Track> templist1 = l1.getTrackByNameList(null);
        for (int i = 0; i < templist1.size(); i++) {
            if (i == 0) {
                Assert.assertEquals("RL 1 Staging 1 Name", "North End 1", templist1.get(i).getName());
            }
            if (i == 1) {
                Assert.assertEquals("RL 1 Staging 2 Name", "North End 2", templist1.get(i).getName());
            }
        }

        l1.register(l1s1);
        l1.register(l1s2);

        Assert.assertEquals("Location 1 Length", 700, l1.getLength());

        Location l2 = new Location("20", "North Industries");
        Assert.assertEquals("Location 2 Id", "20", l2.getId());
        Assert.assertEquals("Location 2 Name", "North Industries", l2.getName());
        l2.setLocationOps(Location.NORMAL);
        l2.setTrainDirections(DIRECTION_ALL);
        l2.setSwitchListEnabled(true);
        lmanager.register(l2);

        Track l2s1 = new Track("20s1", "NI Yard", Track.YARD, l2);
        l2s1.setLength(432);
        Assert.assertEquals("Location 2s1 Id", "20s1", l2s1.getId());
        Assert.assertEquals("Location 2s1 Name", "NI Yard", l2s1.getName());
        Assert.assertEquals("Location 2s1 LocType", Track.YARD, l2s1.getTrackType());
        Assert.assertEquals("Location 2s1 Length", 432, l2s1.getLength());
        l2s1.setTrainDirections(DIRECTION_ALL);

        l2.register(l2s1);
        Assert.assertEquals("Location 2 Length", 432, l2.getLength());

        Location l3 = new Location("3", "South End");
        Assert.assertEquals("Location 3 Id", "3", l3.getId());
        Assert.assertEquals("Location 3 Name", "South End", l3.getName());
        Assert.assertEquals("Location 3 Initial Length", 0, l3.getLength());
        l3.setLocationOps(Location.STAGING);
        l3.setTrainDirections(DIRECTION_ALL);
        l3.setSwitchListEnabled(true);
        lmanager.register(l3);

        Track l3s1 = new Track("3s1", "South End 1", Track.STAGING, l3);
        l3s1.setLength(300);
        Assert.assertEquals("Location 3s1 Id", "3s1", l3s1.getId());
        Assert.assertEquals("Location 3s1 Name", "South End 1", l3s1.getName());
        Assert.assertEquals("Location 3s1 LocType", "Staging", l3s1.getTrackType());
        Assert.assertEquals("Location 3s1 Length", 300, l3s1.getLength());
        l3s1.setTrainDirections(DIRECTION_ALL);
        l3s1.setRoadOption(Track.ALL_ROADS);
        l3s1.setDropOption(Track.ANY);
        l3s1.setPickupOption(Track.ANY);

        Track l3s2 = new Track("3s2", "South End 2", Track.STAGING, l3);
        l3s2.setLength(401);
        Assert.assertEquals("Location 3s2 Id", "3s2", l3s2.getId());
        Assert.assertEquals("Location 3s2 Name", "South End 2", l3s2.getName());
        Assert.assertEquals("Location 3s2 LocType", "Staging", l3s2.getTrackType());
        Assert.assertEquals("Location 3s2 Length", 401, l3s2.getLength());
        l3s2.setTrainDirections(DIRECTION_ALL);
        l3s2.setRoadOption(Track.ALL_ROADS);
        l3s2.setDropOption(Track.ANY);
        l3s2.setPickupOption(Track.ANY);

        l3.addTrack("South End 1", Track.STAGING);
        l3.addTrack("South End 2", Track.STAGING);
        List<Track> templist3 = l3.getTrackByNameList(null);
        for (int i = 0; i < templist3.size(); i++) {
            if (i == 0) {
                Assert.assertEquals("RL 3 Staging 1 Name", "South End 1", templist3.get(i).getName());
            }
            if (i == 1) {
                Assert.assertEquals("RL 3 Staging 2 Name", "South End 2", templist3.get(i).getName());
            }
        }

        l3.register(l3s1);
        l3.register(l3s2);

        Assert.assertEquals("Location 3 Length", 701, l3.getLength());

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // Define the route.
        Route r1 = new Route("1", "Southbound Main Route");
        Assert.assertEquals("Route Id", "1", r1.getId());
        Assert.assertEquals("Route Name", "Southbound Main Route", r1.getName());

        RouteLocation rl1 = new RouteLocation("1r1", l1);
        rl1.setSequenceNumber(1);
        rl1.setTrainDirection(RouteLocation.SOUTH);
        rl1.setMaxCarMoves(5);
        rl1.setMaxTrainLength(1000);
        rl1.setTrainIconX(25); // set the train icon coordinates
        rl1.setTrainIconY(25);

        Assert.assertEquals("Route Location 1 Id", "1r1", rl1.getId());
        Assert.assertEquals("Route Location 1 Name", "North End", rl1.getName());
        RouteLocation rl2 = new RouteLocation("1r2", l2);
        rl2.setSequenceNumber(2);
        rl2.setTrainDirection(RouteLocation.SOUTH);
        // test for only 1 pickup and 1 drop
        rl2.setMaxCarMoves(2);
        rl2.setMaxTrainLength(1000);
        rl2.setTrainIconX(75); // set the train icon coordinates
        rl2.setTrainIconY(25);

        Assert.assertEquals("Route Location 2 Id", "1r2", rl2.getId());
        Assert.assertEquals("Route Location 2 Name", "North Industries", rl2.getName());
        RouteLocation rl3 = new RouteLocation("1r3", l3);
        rl3.setSequenceNumber(3);
        rl3.setTrainDirection(RouteLocation.SOUTH);
        rl3.setMaxCarMoves(5);
        rl3.setMaxTrainLength(1000);
        rl3.setTrainIconX(125); // set the train icon coordinates
        rl3.setTrainIconY(35);

        Assert.assertEquals("Route Location 3 Id", "1r3", rl3.getId());
        Assert.assertEquals("Route Location 3 Name", "South End", rl3.getName());

        r1.register(rl1);
        r1.register(rl2);
        r1.register(rl3);

        rmanager.register(r1);

        // Finally ready to define the trains.
        Train train1 = new Train("1", "STF");
        Assert.assertEquals("Train Id", "1", train1.getId());
        Assert.assertEquals("Train Name", "STF", train1.getName());
        train1.setRoute(r1);
        tmanager.register(train1);

        Train train2 = new Train("2", "SFF");
        Assert.assertEquals("Train Id", "2", train2.getId());
        Assert.assertEquals("Train Name", "SFF", train2.getName());
        train2.setRoute(r1);
        tmanager.register(train2);

        // Last minute checks.
        Assert.assertEquals("Train 1 Departs Name", "North End", train1.getTrainDepartsName());
        Assert.assertEquals("Train 1 Route Departs Name", "North End", train1.getTrainDepartsRouteLocation()
                .getName());
        Assert.assertEquals("Train 1 Terminates Name", "South End", train1.getTrainTerminatesName());
        Assert.assertEquals("Train 1 Route Terminates Name", "South End", train1
                .getTrainTerminatesRouteLocation().getName());
        Assert.assertEquals("Train 1 Next Location Name", "", train1.getNextLocationName());
        Assert.assertEquals("Train 1 Route Name", "Southbound Main Route", train1.getRoute().getName());

        Assert.assertEquals("Train 2 Departs Name", "North End", train2.getTrainDepartsName());
        Assert.assertEquals("Train 2 Route Departs Name", "North End", train2.getTrainDepartsRouteLocation()
                .getName());
        Assert.assertEquals("Train 2 Terminates Name", "South End", train2.getTrainTerminatesName());
        Assert.assertEquals("Train 2 Route Terminates Name", "South End", train2
                .getTrainTerminatesRouteLocation().getName());
        Assert.assertEquals("Train 2 Next Location Name", "", train2.getNextLocationName());
        Assert.assertEquals("Train 2 Route Name", "Southbound Main Route", train2.getRoute().getName());

        // disable build messages
        tmanager.setBuildMessagesEnabled(false);
        // disable build reports
        tmanager.setBuildReportEnabled(false);

        train1.build();
        train2.build();
        Assert.assertEquals("Train 1 after build", true, train1.isBuilt());
        Assert.assertEquals("Train 2 after build", true, train2.isBuilt());
        // check train icon location and name
        TrainIcon ti1 = train1.getTrainIcon();
        Assert.assertNotNull("Train 1 icon exists", ti1);
        Assert.assertEquals("Train 1 icon text", "STF 5016", ti1.getText());
        TrainIcon ti2 = train2.getTrainIcon();
        Assert.assertNotNull("Train 2 icon exists", ti2);
        Assert.assertEquals("Train 2 icon text", "SFF 5524", ti2.getText());

        // icon uses TrainIconAnimation 2 pixels every 3 mSec
        // X=0 to X=25 25/2 * 3 = 38 mSec
        // Y=0 to Y=25 25/2 * 3 = 38 mSec
        // need to wait for icon to finish moving
        jmri.util.JUnitUtil.waitFor(() -> (ti2.getX() == 25 && ti2.getY() == 25), "Train 2 Move 0");

        Assert.assertEquals("Train 1 icon X", 25, ti1.getX());
        Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
        Assert.assertEquals("Train 2 icon X", 25, ti2.getX());
        Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());

        // move the trains
        train1.move();

        // icon uses TrainIconAnimation 2 pixels every 3 mSec
        // need to wait for icon to finish moving
        jmri.util.JUnitUtil.waitFor(() -> (ti1.getX() == 75 && ti1.getY() == 25), "Train 1 Move");

        Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
        Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
        // train 2 shouldn't move
        Assert.assertEquals("Train 2 icon X", 25, ti2.getX());
        Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());

        train2.move();

        // need to wait for icon to finish moving
        jmri.util.JUnitUtil.waitFor(() -> (ti2.getX() == 75 && ti2.getY() == 25), "Train 2 Move");

        Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
        Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
        Assert.assertEquals("Train 2 icon X", 75, ti2.getX());
        Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());

        train2.move();

        // need to wait for icon to finish moving
        jmri.util.JUnitUtil.waitFor(() -> (ti2.getX() == 125 && ti2.getY() == 35), "Train 2 Move 2");

        Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
        Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
        Assert.assertEquals("Train 2 icon X", 125, ti2.getX());
        Assert.assertEquals("Train 2 icon Y", 35, ti2.getY());

        JUnitUtil.dispose(editor.getTargetFrame());
        JUnitUtil.dispose(editor);
    }

    //    private final static Logger log = LoggerFactory.getLogger(TrainIconAnimationTest.class);

}
