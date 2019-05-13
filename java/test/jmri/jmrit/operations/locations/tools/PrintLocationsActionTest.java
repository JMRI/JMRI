package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.tools.PrintLocationsAction.LocationPrintOptionFrame;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintLocationsActionTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintLocationsAction t = new PrintLocationsAction("test action", true);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPrintPreview() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        JUnitOperationsUtil.createSchedules(); // increase coverage by adding a schedule
        Location location = JUnitOperationsUtil.createOneNormalLocation("TEST_LOCATION"); // increase coverage
        Track interchange1 = location.getTrackByName("TEST_LOCATION Interchange 1", null);
        Assert.assertNotNull("confirm track exists", interchange1);
        Track interchange2 = location.getTrackByName("TEST_LOCATION Interchange 2", null);
        Assert.assertNotNull("confirm track exists", interchange2);
        
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.getTrainByName("STF");
        
        interchange1.setDropOption(Track.TRAINS);
        interchange1.addDropId(train.getId());
        interchange1.setPickupOption(Track.EXCLUDE_TRAINS);
        interchange1.addPickupId(train.getId());
        
        interchange1.setRoadOption(Track.INCLUDE_ROADS);
        interchange1.addRoadName("SP");
        
        interchange1.setLoadOption(Track.INCLUDE_LOADS);
        interchange1.addLoadName("Bolts");
        
        interchange1.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        interchange1.addDestination(location);
        
        interchange1.setServiceOrder(Track.LIFO);
        
        interchange2.setDropOption(Track.ROUTES);
        interchange2.addDropId(train.getRoute().getId());
        interchange2.setPickupOption(Track.EXCLUDE_ROUTES);
        interchange2.addPickupId(train.getRoute().getId());
        
        interchange2.setRoadOption(Track.EXCLUDE_ROADS);
        interchange2.addRoadName("SP");
        
        interchange2.setLoadOption(Track.EXCLUDE_LOADS);
        interchange2.addLoadName("Bolts");
        
        interchange2.setDestinationOption(Track.EXCLUDE_DESTINATIONS);
        interchange2.addDestination(location);
        
        interchange2.setServiceOrder(Track.FIFO);
        
        // staging options
        Location staging = InstanceManager.getDefault(LocationManager.class).getLocationByName("North End Staging");
        Track stagingTrack = staging.getTrackByName("North End 1", null);
        Assert.assertNotNull("exists", stagingTrack);
        
        stagingTrack.setAddCustomLoadsAnySpurEnabled(true);
        stagingTrack.setAddCustomLoadsAnyStagingTrackEnabled(true);
        stagingTrack.setAddCustomLoadsEnabled(true);
        stagingTrack.setBlockCarsEnabled(true);
        stagingTrack.setRemoveCustomLoadsEnabled(true);    
        stagingTrack.setLoadSwapEnabled(true);
        stagingTrack.setLoadEmptyEnabled(true);
        
        stagingTrack.setShipLoadOption(Track.EXCLUDE_LOADS);
        stagingTrack.addShipLoadName("Screws");

        PrintLocationsAction pla = new PrintLocationsAction("test action", true);
        Assert.assertNotNull("exists", pla);
        // select all options
        pla.printLocations.setSelected(true);
        pla.printSchedules.setSelected(true);
        pla.printComments.setSelected(true);
        pla.printDetails.setSelected(true);
        pla.printAnalysis.setSelected(true);
        pla.printErrorAnalysis.setSelected(true);
        pla.printLocations();
        
        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + "Locations");
        
        Assert.assertNotNull("exists", printPreviewFrame);
        JUnitUtil.dispose(printPreviewFrame);
    }

    @Test
    public void testPrintOptionsFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        PrintLocationsAction pla = new PrintLocationsAction("test action", true);
        Assert.assertNotNull("exists", pla);

        LocationPrintOptionFrame f = pla.new LocationPrintOptionFrame(pla);
        Assert.assertNotNull("exists", f);

        JemmyUtil.enterClickAndLeave(f.okayButton); // closes window

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + Bundle.getMessage("TitleLocationsTable"));
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(f);
        JUnitUtil.dispose(printPreviewFrame);
    }

    @Test
    public void testActionPerformed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");

        PrintLocationsAction pla = new PrintLocationsAction("test action", true, location);
        Assert.assertNotNull("exists", pla);

        pla.actionPerformed(new ActionEvent("Test Action", 0, null));

        // confirm print option window is showing
        LocationPrintOptionFrame printOptionFrame =
                (LocationPrintOptionFrame) JmriJFrame.getFrame(Bundle.getMessage("MenuItemPreview"));
        Assert.assertNotNull("exists", printOptionFrame);

        JemmyUtil.enterClickAndLeave(printOptionFrame.okayButton); // closes window

        // confirm print preview window is showing
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.util.UtilBundle");
        JmriJFrame printPreviewFrame =
                JmriJFrame.getFrame(rb.getString("PrintPreviewTitle") + " " + location.getName());
        Assert.assertNotNull("exists", printPreviewFrame);

        JUnitUtil.dispose(printPreviewFrame);

        JUnitUtil.dispose(printOptionFrame);
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintLocationsActionTest.class);

}
