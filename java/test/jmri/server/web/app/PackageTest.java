package jmri.server.web.app;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Randall Wood (C) 2016
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    JsonMenuItemTest.class,
    WebAppConfigurationTest.class,
    JsonManifestTest.class,
    WebAppManagerTest.class,
    WebAppServletTest.class
})
public class PackageTest {
}
