package jmri.server.web;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author Randall Wood (C) 2016
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    jmri.server.web.app.BundleTest.class,
    jmri.server.web.spi.WebServerConfigurationTest.class,
    AbstractWebServerConfigurationTest.class,
    DefaultWebServerConfigurationTest.class
})
public class PackageTest {

}
