package jmri.jmrix.can.cbus.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.jmrix.can.cbus.swing.CbusMenuTest.class,
    jmri.jmrix.can.cbus.swing.CbusComponentFactoryTest.class,
    jmri.jmrix.can.cbus.swing.eventtable.PackageTest.class,
    jmri.jmrix.can.cbus.swing.nodeconfig.PackageTest.class,
    jmri.jmrix.can.cbus.swing.console.PackageTest.class,
    jmri.jmrix.can.cbus.swing.configtool.PackageTest.class,
    jmri.jmrix.can.cbus.swing.cbusslotmonitor.PackageTest.class,
    jmri.jmrix.can.cbus.swing.simulator.PackageTest.class,
    jmri.jmrix.can.cbus.swing.CbusFilterFrameTest.class,
    jmri.jmrix.can.cbus.swing.CbusFilterPanelTest.class,
    jmri.jmrix.can.cbus.swing.eventrequestmonitor.PackageTest.class,
    jmri.jmrix.can.cbus.swing.CbusEventHighlightFrameTest.class,
    jmri.jmrix.can.cbus.swing.CbusEventHighlightPanelTest.class,
    BundleTest.class
})

/**
 * Tests for the jmri.jmrix.can.cbus.swing package
 *
 * @author  Paul Bender	Copyright (C) 2016
 */
public class PackageTest{
}
