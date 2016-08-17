package jmri.server.json;

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
    jmri.server.json.light.PackageTest.class,
    jmri.server.json.memory.PackageTest.class,
    jmri.server.json.power.PackageTest.class,
    jmri.server.json.reporter.PackageTest.class,
    jmri.server.json.route.PackageTest.class,
    jmri.server.json.roster.PackageTest.class,
    jmri.server.json.sensor.PackageTest.class,
    jmri.server.json.turnout.PackageTest.class,
    jmri.server.json.throttle.PackageTest.class,
    jmri.server.json.util.PackageTest.class
})
public class PackageTest {
}
