package jmri.server.json.route;

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
    JsonRouteHttpServiceTest.class,
    JsonRouteSocketServiceTest.class
})
public class PackageTest {
}
