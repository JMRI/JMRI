package jmri.jmrit.operations.locations.gui;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class YardEditFrameTest extends OperationsTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;
    private LocationManager lManager;
    private Location l;

    @Test
    public void testCreateYardTrackDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Add Frame");
        f.initComponents(l, null);

        // create a yard track with length 43.
        f.trackNameTextField.setText("new yard track");
        f.trackLengthTextField.setText("43");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("new yard track", null);
        Assert.assertNotNull("new yard track", t);
        Assert.assertEquals("yard track length", 43, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add a second track with length 6543.
        f.trackNameTextField.setText("2nd yard track");
        f.trackLengthTextField.setText("6543");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        t = l.getTrackByName("2nd yard track", null);
        Assert.assertNotNull("2nd yard track", t);
        Assert.assertEquals("2nd yard track length", 6543, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // add A third track with length 1.
        f.trackNameTextField.setText("3rd yard track");
        f.trackLengthTextField.setText("1");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        t = l.getTrackByName("3rd yard track", null);
        Assert.assertNotNull("3rd yard track", t);
        Assert.assertEquals("3rd yard track length", 1, t.getLength());
        // check that the defaults are correct
        Assert.assertEquals("all directions", ALL, t.getTrainDirections());
        Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

        // clean up the frame
        f.setVisible(false);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetDirectionUsingCheckbox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Direction Set Frame");
        f.initComponents(l, null);

        f.trackNameTextField.setText("4th yard track");
        f.trackLengthTextField.setText("21");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        Track t = l.getTrackByName("4th yard track", null);
        Assert.assertNotNull("4th yard track", t);
        Assert.assertEquals("4th yard track length", 21, t.getLength());
        Assert.assertEquals("Direction all before change", ALL, t.getTrainDirections());

        // deselect east, west and south check boxes
        JemmyUtil.enterClickAndLeave(f.eastCheckBox);
        JemmyUtil.enterClickAndLeave(f.westCheckBox);
        JemmyUtil.enterClickAndLeave(f.southCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveTrackButton);

        Assert.assertEquals("only north", Track.NORTH, t.getTrainDirections());

        // clean up the frame
        f.setVisible(false);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCreateTracksAndReloadFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame f = new YardEditFrame();
        f.setTitle("Test Yard Create Frame");
        f.initComponents(l, null);

        // create four yard tracks
        f.trackNameTextField.setText("new yard track");
        f.trackLengthTextField.setText("43");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("2nd yard track");
        f.trackLengthTextField.setText("6543");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("3rd yard track");
        f.trackLengthTextField.setText("1");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        f.trackNameTextField.setText("4th yard track");
        f.trackLengthTextField.setText("21");
        JemmyUtil.enterClickAndLeave(f.addTrackButton);

        // deselect east, west and south check boxes
        JemmyUtil.enterClickAndLeave(f.eastCheckBox);
        JemmyUtil.enterClickAndLeave(f.westCheckBox);
        JemmyUtil.enterClickAndLeave(f.southCheckBox);

        JemmyUtil.enterClickAndLeave(f.saveTrackButton);

        // clean up the frame
        f.setVisible(false);
        JUnitUtil.dispose(f);

        // now reload
        Location l2 = lManager.getLocationByName("Test Loc C");
        Assert.assertNotNull("Location Test Loc C", l2);

        LocationEditFrame fl = new LocationEditFrame(l2);
        fl.setTitle("Test Edit Restored Location Frame");

        // check location name
        Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());

        Assert.assertEquals("number of yards", 4, fl.yardModel.getRowCount());
        Assert.assertEquals("number of staging tracks", 0, fl.stagingModel.getRowCount());

        // clean up the frame
        fl.setVisible(false);
        JUnitUtil.dispose(fl);
    }

    @Test
    public void testDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Delete", Track.YARD);
        YardEditFrame f = new YardEditFrame();
        f.initComponents(l, t);

        JemmyUtil.enterClickAndLeave(f.deleteTrackButton);
        t = l.getTrackByName("Test Yard Delete", null);
        Assert.assertNull("track should not exist", t);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testLIFO() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Order", Track.YARD);
        YardEditFrame f = new YardEditFrame();
        f.initComponents(l, t);

        JemmyUtil.enterClickAndLeave(f.orderLIFO);
        Assert.assertEquals("service order", Track.LIFO, t.getServiceOrder());
        JemmyUtil.enterClickAndLeave(f.orderFIFO);
        Assert.assertEquals("service order", Track.FIFO, t.getServiceOrder());
        JemmyUtil.enterClickAndLeave(f.orderNormal);
        Assert.assertEquals("service order", Track.NORMAL, t.getServiceOrder());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testErrorTrackName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame f = new YardEditFrame();
        f.initComponents(l, null);

        // no track name entered
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addTrackButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("CanNotTrack"), new Object[]{Bundle.getMessage("add")}),
                Bundle.getMessage("ButtonOK"));

        // hyphen feature requires at least 2 characters
        f.trackNameTextField.setText("-");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addTrackButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("CanNotTrack"), new Object[]{Bundle.getMessage("add")}),
                Bundle.getMessage("ButtonOK"));
        
        // track name too long (25 characters)
        f.trackNameTextField.setText("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addTrackButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("CanNotTrack"), new Object[]{Bundle.getMessage("add")}),
                Bundle.getMessage("ButtonOK"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testErrorTrackLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame f = new YardEditFrame();
        f.initComponents(l, null);

        // the length field is empty
        f.trackNameTextField.setText("new yard track");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addTrackButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"), Bundle.getMessage("ButtonOK"));
        
        // bad inches conversion
        f.trackLengthTextField.setText("A\"");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveTrackButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"), Bundle.getMessage("ButtonOK"));

        // bad centimeter conversion
        f.trackLengthTextField.setText("Acm");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveTrackButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"), Bundle.getMessage("ButtonOK"));

        // too large of a number
        f.trackLengthTextField.setText("100000");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveTrackButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"), Bundle.getMessage("ButtonOK"));

        f.trackLengthTextField.setText("300");
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        Track t = l.getTrackByName("new yard track", null);
        
        // place a car on track
        JUnitOperationsUtil.createAndPlaceCar("CP", "X10001", "Boxcar", "40", "DAB", "1984", t, 0);
        
        // track is too short for a 40 foot car, need room for couplers
        f.trackLengthTextField.setText("40");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveTrackButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"), Bundle.getMessage("ButtonOK"));
        // force track to 40 feet
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"), Bundle.getMessage("ButtonNo"));
        Assert.assertEquals("track length", 300, t.getLength());
        
        // again, but say yes this time
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveTrackButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"), Bundle.getMessage("ButtonOK"));
        // force track to 40 feet
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"), Bundle.getMessage("ButtonYes"));
        Assert.assertEquals("track length", 40, t.getLength());
        
        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrackLengthInches() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Length", Track.YARD);
        YardEditFrame f = new YardEditFrame();
        f.initComponents(l, t);

        f.trackLengthTextField.setText("24\"");
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        // confirm HO default
        Assert.assertEquals("ratio", 87, Setup.getScaleRatio());
        Assert.assertEquals("track length", 174, t.getLength());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testTrackLengthCentimeters() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Yard Length", Track.YARD);
        YardEditFrame f = new YardEditFrame();
        f.initComponents(l, t);

        f.trackLengthTextField.setText("240cm");
        JemmyUtil.enterClickAndLeave(f.saveTrackButton);
        // confirm HO default
        Assert.assertEquals("ratio", 87, Setup.getScaleRatio());
        // length conversion 240 x 87 / 100
        Assert.assertEquals("track length", 208, t.getLength());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testTypes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Car Types", Track.YARD);
        t.setLength(100);
        YardEditFrame f = new YardEditFrame();
        f.initComponents(l, t);
        
        Assert.assertTrue("Boxcar is accepted", t.isTypeNameAccepted("Boxcar"));

        JFrameOperator jfo = new JFrameOperator(f);
        JCheckBoxOperator jbo = new JCheckBoxOperator(jfo, "Boxcar");
        jbo.doClick();
        
        Assert.assertFalse("Boxcar is not accepted", t.isTypeNameAccepted("Boxcar"));
        
        jbo.doClick();
        Assert.assertTrue("Boxcar is accepted", t.isTypeNameAccepted("Boxcar"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Track t = l.addTrack("Test Close", Track.YARD);
        YardEditFrame f = new YardEditFrame();
        f.initComponents(l, t);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // Ensure minimal setup for log4J
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        JUnitOperationsUtil.loadFiveLocations();

        lManager = InstanceManager.getDefault(LocationManager.class);
        l = lManager.getLocationByName("Test Loc C");

        JUnitOperationsUtil.loadTrain(l);
    }
}
