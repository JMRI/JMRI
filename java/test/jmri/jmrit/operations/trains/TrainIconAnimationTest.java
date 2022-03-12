package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainIconAnimationTest extends OperationsTestCase {

//    private final int DIRECTION_ALL = Location.EAST + Location.WEST + Location.NORTH + Location.SOUTH;

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
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);

        // create and register a panel
        jmri.jmrit.display.panelEditor.PanelEditor editor = new jmri.jmrit.display.panelEditor.PanelEditor(
                "Train Test Panel");
        InstanceManager.getDefault(EditorManager.class).add(editor);

        // confirm panel creation
        JmriJFrame f = JmriJFrame.getFrame("Train Test Panel");
        Assert.assertNotNull(f);

        // Place train icons on panel
        Setup.setPanelName("Train Test Panel");
        // icon color when traveling south
        Setup.setTrainIconColorSouth(TrainIcon.BLUE);
        // icon color when traveling east
        Setup.setTrainIconColorEast(TrainIcon.GREEN);
        // icon color when traveling west
        Setup.setTrainIconColorWest(TrainIcon.RED);
        // Set terminate color to yellow
        Setup.setTrainIconColorTerminate(TrainIcon.YELLOW);
        // add engine number
        Setup.setTrainIconAppendEnabled(true);
        
        JUnitOperationsUtil.initOperationsData();

        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        Track northEndStaging2 = locationNorthEnd.getTrackById("1s2");
        
        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(locationNorthEnd, northEndStaging1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(locationNorthEnd, northEndStaging1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(locationNorthEnd, northEndStaging2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(locationNorthEnd, northEndStaging2));

        // get trains
        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        
        Route route = train2.getRoute();
        RouteLocation rl2 = route.getRouteLocationBySequenceNumber(2);
        rl2.setTrainDirection(RouteLocation.EAST);
        RouteLocation rl3 = route.getRouteLocationBySequenceNumber(3);
        rl3.setTrainDirection(RouteLocation.WEST);
        rl3.setTrainIconY(35);

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
        Assert.assertEquals("Train 1 color", LocoIcon.COLOR_BLUE, ti1.getLocoColor());
        Assert.assertEquals("Train 2 icon X", 25, ti2.getX());
        Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());

        // move the trains
        train1.move();

        // icon uses TrainIconAnimation 2 pixels every 3 mSec
        // need to wait for icon to finish moving
        jmri.util.JUnitUtil.waitFor(() -> (ti1.getX() == 75 && ti1.getY() == 25), "Train 1 Move 1");

        Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
        Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
        Assert.assertEquals("Train 1 color", Color.GREEN, ti1.getLocoColor());
        // train 2 shouldn't move
        Assert.assertEquals("Train 2 icon X", 25, ti2.getX());
        Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());
        Assert.assertEquals("Train 2 color", LocoIcon.COLOR_BLUE, ti2.getLocoColor());

        train2.move();

        // need to wait for icon to finish moving
        jmri.util.JUnitUtil.waitFor(() -> (ti2.getX() == 75 && ti2.getY() == 25), "Train 2 Move 1");

        Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
        Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
        Assert.assertEquals("Train 2 icon X", 75, ti2.getX());
        Assert.assertEquals("Train 2 icon Y", 25, ti2.getY());
        Assert.assertEquals("Train 2 color", Color.GREEN, ti2.getLocoColor());

        train2.move();

        // need to wait for icon to finish moving
        jmri.util.JUnitUtil.waitFor(() -> (ti2.getX() == 125 && ti2.getY() == 35), "Train 2 Move 2");

        Assert.assertEquals("Train 1 icon X", 75, ti1.getX());
        Assert.assertEquals("Train 1 icon Y", 25, ti1.getY());
        Assert.assertEquals("Train 1 color", Color.GREEN, ti1.getLocoColor());
        Assert.assertEquals("Train 2 icon X", 125, ti2.getX());
        Assert.assertEquals("Train 2 icon Y", 35, ti2.getY());
        Assert.assertEquals("Train 2 color", Color.RED, ti2.getLocoColor());
        
        // terminate
        train2.move();
        Assert.assertEquals("Train 2 color", Color.YELLOW, ti2.getLocoColor());

        JUnitUtil.dispose(editor.getTargetFrame());
        JUnitUtil.dispose(editor);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    //    private final static Logger log = LoggerFactory.getLogger(TrainIconAnimationTest.class);

}
