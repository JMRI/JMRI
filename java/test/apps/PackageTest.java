package apps;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        ConfigBundleTest.class,
        ValidateConfigFilesTest.class,
        apps.configurexml.PackageTest.class,
        apps.startup.PackageTest.class,
        apps.PacketPro.PackageTest.class,
        apps.PacketScript.PackageTest.class,
        apps.InstallTest.PackageTest.class,
        apps.gui3.Gui3AppsTest.class,
        apps.DecoderPro.PackageTest.class,
        apps.JmriDemo.PackageTest.class,
        apps.DispatcherPro.PackageTest.class,
        apps.PanelPro.PackageTest.class,
        apps.SignalPro.PackageTest.class,
        JmriFacelessTest.class
})
/**
 * Invoke complete set of tests for the apps package
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002, 2007, 2012
 * @author      Paul Bender, Copyright (C) 2016
 */
public class PackageTest {

}
