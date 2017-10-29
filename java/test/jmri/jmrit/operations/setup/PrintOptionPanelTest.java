package jmri.jmrit.operations.setup;

import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PrintOptionPanelTest {

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
        Assert.assertFalse(p.printLoadsEmptiesCheckBox.isSelected());
        Assert.assertFalse(p.printTimetableNameCheckBox.isSelected());
        Assert.assertTrue(p.printValidCheckBox.isSelected());
        Assert.assertFalse(p.sortByTrackCheckBox.isSelected());
        Assert.assertFalse(p.printHeadersCheckBox.isSelected());
        Assert.assertFalse(p.truncateCheckBox.isSelected());
        Assert.assertFalse(p.departureTimeCheckBox.isSelected());
        Assert.assertTrue(p.trackSummaryCheckBox.isSelected());
        Assert.assertTrue(p.routeLocationCheckBox.isSelected());

    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitOperationsUtil.resetOperationsManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PrintOptionPanelTest.class);

}
