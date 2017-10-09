package jmri.server.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Randall Wood 2016, 2017
 */
@RunWith(Suite.class)
@SuiteClasses({
    jmri.server.json.block.PackageTest.class,
    jmri.server.json.consist.PackageTest.class,
    jmri.server.json.layoutblock.PackageTest.class,
    jmri.server.json.light.PackageTest.class,
    jmri.server.json.logs.PackageTest.class,
    jmri.server.json.memory.PackageTest.class,
    jmri.server.json.operations.PackageTest.class,
    jmri.server.json.power.PackageTest.class,
    jmri.server.json.reporter.PackageTest.class,
    jmri.server.json.route.PackageTest.class,
    jmri.server.json.roster.PackageTest.class,
    jmri.server.json.sensor.PackageTest.class,
    jmri.server.json.signalHead.PackageTest.class,
    jmri.server.json.signalMast.PackageTest.class,
    jmri.server.json.time.PackageTest.class,
    jmri.server.json.turnout.PackageTest.class,
    jmri.server.json.throttle.PackageTest.class,
    jmri.server.json.util.PackageTest.class,
    BundleTest.class,
    JsonExceptionTest.class,
    JsonClientHandlerTest.class,
    JsonConnectionTest.class,
    JsonWebSocketTest.class
})
public class PackageTest {
}
