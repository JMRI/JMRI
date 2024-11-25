package jmri.jmrit.operations.setup;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PrintOptionPanelTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        PrintOptionPanel t = new PrintOptionPanel();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void checkPrintOptionPanelDefaults() {
        PrintOptionPanel p = new PrintOptionPanel();

        // confirm defaults
        Assert.assertFalse(p.tabFormatCheckBox.isSelected());
        Assert.assertTrue(p.formatSwitchListCheckBox.isSelected());
        Assert.assertFalse(p.editManifestCheckBox.isSelected());
        Assert.assertFalse(p.printLocCommentsCheckBox.isSelected());
        Assert.assertFalse(p.printRouteCommentsCheckBox.isSelected());
        Assert.assertFalse(p.printCabooseLoadCheckBox.isSelected());
        Assert.assertFalse(p.printPassengerLoadCheckBox.isSelected());
        Assert.assertFalse(p.printLoadsEmptiesCheckBox.isSelected());
        Assert.assertFalse(p.printTrainScheduleNameCheckBox.isSelected());
        Assert.assertTrue(p.printValidCheckBox.isSelected());
        Assert.assertFalse(p.sortByTrackCheckBox.isSelected());
        Assert.assertFalse(p.printHeadersCheckBox.isSelected());
        Assert.assertTrue(p.printPageHeaderCheckBox.isSelected());
        Assert.assertFalse(p.truncateCheckBox.isSelected());
        Assert.assertFalse(p.manifestDepartureTimeCheckBox.isSelected());
        Assert.assertTrue(p.trackSummaryCheckBox.isSelected());
        Assert.assertTrue(p.routeLocationCheckBox.isSelected());
        Assert.assertFalse(p.switchListDepartureTimeCheckBox.isSelected());
        Assert.assertFalse(p.groupCarMovesCheckBox.isSelected());
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintOptionPanelTest.class);

}
