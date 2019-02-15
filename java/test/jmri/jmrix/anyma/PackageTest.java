package jmri.jmrix.anyma;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    AnymaDMX_ConnectionConfigTest.class,
    AnymaDMX_ConnectionTypeListTest.class,
    AnymaDMX_SystemConnectionMemoTest.class,
    AnymaDMX_TrafficControllerTest.class,
    AnymaDMX_UsbLightTest.class,
    AnymaDMX_UsbPortAdapterTest.class,
    UsbLightManagerTest.class,
    jmri.jmrix.anyma.configurexml.PackageTest.class,
    BundleTest.class,
    })

/**
 * Tests for the jmri.jmrix.acela.configurexml package.
 *
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class PackageTest {
}
