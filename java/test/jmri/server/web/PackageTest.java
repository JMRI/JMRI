package jmri.server.web;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Randall Wood (C) 2016, 2017
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.server.web.app.PackageTest.class,
    jmri.server.web.spi.PackageTest.class,
    AbstractWebServerConfigurationTest.class,
    DefaultWebServerConfigurationTest.class,
    BundleTest.class
})
public class PackageTest {
}
