package jmri.jmrit.operations.setup.gui;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintOptionFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame t = new PrintOptionFrame();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testPrintOptionFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame f = new PrintOptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        Assert.assertTrue(f.isShowing());

        PrintOptionPanel pop = (PrintOptionPanel) f.getContentPane();
        Assert.assertNotNull("exists", pop);

        // confirm default
        Assert.assertTrue(Setup.isSwitchListFormatSameAsManifest());
        Assert.assertFalse(pop.isDirty());

        // test save button
        JemmyUtil.enterClickAndLeave(pop.formatSwitchListCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);

        Assert.assertFalse(pop.isDirty());
        Assert.assertFalse(Setup.isSwitchListFormatSameAsManifest());

        // done
        JUnitUtil.dispose(f);
    }

    /**
     * Test the three switch list option checkboxes
     */
    @Test
    public void testSwitchListCheckboxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame f = new PrintOptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        Assert.assertTrue(f.isShowing());

        PrintOptionPanel pop = (PrintOptionPanel) f.getContentPane();
        Assert.assertNotNull("exists", pop);

        // confirm defaults
        Assert.assertTrue(Setup.isPrintTrackSummaryEnabled());
        Assert.assertTrue(Setup.isSwitchListRouteLocationCommentEnabled());
        Assert.assertFalse(Setup.isUseSwitchListDepartureTimeEnabled());
        Assert.assertFalse(pop.isDirty());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.trackSummaryCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintTrackSummaryEnabled());
        Assert.assertTrue(Setup.isSwitchListRouteLocationCommentEnabled());
        Assert.assertFalse(Setup.isUseSwitchListDepartureTimeEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.routeLocationCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintTrackSummaryEnabled());
        Assert.assertFalse(Setup.isSwitchListRouteLocationCommentEnabled());
        Assert.assertFalse(Setup.isUseSwitchListDepartureTimeEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.switchListDepartureTimeCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintTrackSummaryEnabled());
        Assert.assertFalse(Setup.isSwitchListRouteLocationCommentEnabled());
        Assert.assertTrue(Setup.isUseSwitchListDepartureTimeEnabled());

        // done
        JUnitUtil.dispose(f);
    }

    /**
     * Test the four Manifest option checkboxes
     */
    @Test
    public void testManifestCheckboxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame f = new PrintOptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        Assert.assertTrue(f.isShowing());

        PrintOptionPanel pop = (PrintOptionPanel) f.getContentPane();
        Assert.assertNotNull("exists", pop);

        // confirm defaults
        Assert.assertFalse(Setup.isPrintLocationCommentsEnabled());
        Assert.assertFalse(Setup.isPrintRouteCommentsEnabled());
        Assert.assertFalse(Setup.isUseDepartureTimeEnabled());
        Assert.assertFalse(Setup.isPrintTruncateManifestEnabled());
        Assert.assertFalse(pop.isDirty());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printLocCommentsCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertTrue(Setup.isPrintLocationCommentsEnabled());
        Assert.assertFalse(Setup.isPrintRouteCommentsEnabled());
        Assert.assertFalse(Setup.isUseDepartureTimeEnabled());
        Assert.assertFalse(Setup.isPrintTruncateManifestEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printRouteCommentsCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertTrue(Setup.isPrintLocationCommentsEnabled());
        Assert.assertTrue(Setup.isPrintRouteCommentsEnabled());
        Assert.assertFalse(Setup.isUseDepartureTimeEnabled());
        Assert.assertFalse(Setup.isPrintTruncateManifestEnabled());

        // make change and save
        // creates a pop up
        Thread t = new Thread(() -> {
            JemmyUtil.enterClickAndLeave(pop.truncateCheckBox);
        });
        t.start();
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("TruncateManifests?"), Bundle.getMessage("ButtonYes"));
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertTrue(Setup.isPrintLocationCommentsEnabled());
        Assert.assertTrue(Setup.isPrintRouteCommentsEnabled());
        Assert.assertFalse(Setup.isUseDepartureTimeEnabled());
        Assert.assertTrue(Setup.isPrintTruncateManifestEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.manifestDepartureTimeCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertTrue(Setup.isPrintLocationCommentsEnabled());
        Assert.assertTrue(Setup.isPrintRouteCommentsEnabled());
        Assert.assertTrue(Setup.isUseDepartureTimeEnabled());
        Assert.assertTrue(Setup.isPrintTruncateManifestEnabled());

        // done
        JUnitUtil.dispose(f);
    }

    /**
     * Test the twelve Manifest and switch list option checkboxes
     */
    @Test
    public void testManifestSwichListCheckboxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame f = new PrintOptionFrame();
        f.setLocation(0, 0); // entire panel must be visible for tests to work properly
        f.initComponents();

        Assert.assertTrue(f.isShowing());

        PrintOptionPanel pop = (PrintOptionPanel) f.getContentPane();
        Assert.assertNotNull("exists", pop);

        // confirm defaults
        Assert.assertTrue(Setup.isPrintValidEnabled());
        Assert.assertFalse(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertFalse(Setup.isGroupCarMovesEnabled());
        Assert.assertFalse(Setup.isPrintLocoLastEnabled());
        Assert.assertFalse(Setup.isPrintCabooseLoadEnabled());
        Assert.assertFalse(Setup.isPrintPassengerLoadEnabled());

        Assert.assertFalse(Setup.is12hrFormatEnabled());
        Assert.assertFalse(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());
        Assert.assertFalse(pop.isDirty());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printValidCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertFalse(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertFalse(Setup.isGroupCarMovesEnabled());
        Assert.assertFalse(Setup.isPrintLocoLastEnabled());
        Assert.assertFalse(Setup.isPrintCabooseLoadEnabled());
        Assert.assertFalse(Setup.isPrintPassengerLoadEnabled());

        Assert.assertFalse(Setup.is12hrFormatEnabled());
        Assert.assertFalse(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printLoadsEmptiesCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertFalse(Setup.isGroupCarMovesEnabled());
        Assert.assertFalse(Setup.isPrintLocoLastEnabled());
        Assert.assertFalse(Setup.isPrintCabooseLoadEnabled());
        Assert.assertFalse(Setup.isPrintPassengerLoadEnabled());

        Assert.assertFalse(Setup.is12hrFormatEnabled());
        Assert.assertFalse(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.groupCarMovesCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertFalse(Setup.isPrintLocoLastEnabled());
        Assert.assertFalse(Setup.isPrintCabooseLoadEnabled());
        Assert.assertFalse(Setup.isPrintPassengerLoadEnabled());

        Assert.assertFalse(Setup.is12hrFormatEnabled());
        Assert.assertFalse(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printLocoLastCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertFalse(Setup.isPrintCabooseLoadEnabled());
        Assert.assertFalse(Setup.isPrintPassengerLoadEnabled());

        Assert.assertFalse(Setup.is12hrFormatEnabled());
        Assert.assertFalse(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printCabooseLoadCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertTrue(Setup.isPrintCabooseLoadEnabled());
        Assert.assertFalse(Setup.isPrintPassengerLoadEnabled());

        Assert.assertFalse(Setup.is12hrFormatEnabled());
        Assert.assertFalse(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printPassengerLoadCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertTrue(Setup.isPrintCabooseLoadEnabled());
        Assert.assertTrue(Setup.isPrintPassengerLoadEnabled());

        Assert.assertFalse(Setup.is12hrFormatEnabled());
        Assert.assertFalse(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.use12hrFormatCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertTrue(Setup.isPrintCabooseLoadEnabled());
        Assert.assertTrue(Setup.isPrintPassengerLoadEnabled());

        Assert.assertTrue(Setup.is12hrFormatEnabled());
        Assert.assertFalse(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printTrainScheduleNameCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertTrue(Setup.isPrintCabooseLoadEnabled());
        Assert.assertTrue(Setup.isPrintPassengerLoadEnabled());

        Assert.assertTrue(Setup.is12hrFormatEnabled());
        Assert.assertTrue(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertFalse(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.sortByTrackCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertTrue(Setup.isPrintCabooseLoadEnabled());
        Assert.assertTrue(Setup.isPrintPassengerLoadEnabled());

        Assert.assertTrue(Setup.is12hrFormatEnabled());
        Assert.assertTrue(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertTrue(Setup.isSortByTrackNameEnabled());
        Assert.assertTrue(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.noPageBreaksCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertTrue(Setup.isPrintCabooseLoadEnabled());
        Assert.assertTrue(Setup.isPrintPassengerLoadEnabled());

        Assert.assertTrue(Setup.is12hrFormatEnabled());
        Assert.assertTrue(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertTrue(Setup.isSortByTrackNameEnabled());
        Assert.assertFalse(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertFalse(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printHeadersCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertTrue(Setup.isPrintCabooseLoadEnabled());
        Assert.assertTrue(Setup.isPrintPassengerLoadEnabled());

        Assert.assertTrue(Setup.is12hrFormatEnabled());
        Assert.assertTrue(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertTrue(Setup.isSortByTrackNameEnabled());
        Assert.assertFalse(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertTrue(Setup.isPrintHeadersEnabled());
        Assert.assertTrue(Setup.isPrintPageHeaderEnabled());

        // make change and save
        JemmyUtil.enterClickAndLeave(pop.printPageHeaderCheckBox);
        Assert.assertTrue(pop.isDirty());
        JemmyUtil.enterClickAndLeave(pop.saveButton);
        Assert.assertFalse(pop.isDirty());

        Assert.assertFalse(Setup.isPrintValidEnabled());
        Assert.assertTrue(Setup.isPrintLoadsAndEmptiesEnabled());
        Assert.assertTrue(Setup.isGroupCarMovesEnabled());
        Assert.assertTrue(Setup.isPrintLocoLastEnabled());
        Assert.assertTrue(Setup.isPrintCabooseLoadEnabled());
        Assert.assertTrue(Setup.isPrintPassengerLoadEnabled());

        Assert.assertTrue(Setup.is12hrFormatEnabled());
        Assert.assertTrue(Setup.isPrintTrainScheduleNameEnabled());
        Assert.assertTrue(Setup.isSortByTrackNameEnabled());
        Assert.assertFalse(Setup.isPrintNoPageBreaksEnabled());
        Assert.assertTrue(Setup.isPrintHeadersEnabled());
        Assert.assertFalse(Setup.isPrintPageHeaderEnabled());

        // done
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PrintOptionFrame f = new PrintOptionFrame();
        f.initComponents();
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintOptionFrameTest.class);

}
