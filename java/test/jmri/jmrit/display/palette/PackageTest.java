package jmri.jmrit.display.palette;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmrit.display.paneleditor package
 *
 * @author	Bob Jacobsen Copyright 2008, 2009, 2010, 2015
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    ItemPaletteTest.class,
    BundleTest.class,
    BackgroundItemPanelTest.class,
    ClockItemPanelTest.class,
    IconItemPanelTest.class,
    IndicatorItemPanelTest.class,
    PortalItemPanelTest.class,
    RPSItemPanelTest.class,
    TextItemPanelTest.class
})
public class PackageTest {
}
