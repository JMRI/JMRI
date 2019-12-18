package jmri.server.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test for the JSON server.
 * <p>
 * <strong>Note:</strong> Creating unit tests for the constructors of classes in
 * this package and subpackages that are
 * <em>not</em> tested as part of
 * {@link jmri.server.json.schema.JsonSchemaServiceCacheTest} or
 * {@link jmri.spi.JsonServiceFactoryTest} will mask the fact that the
 * {@link jmri.spi.JsonServiceFactory} registered as a
 * {@link org.openide.util.lookup.ServiceProvider} is not registered correctly,
 * such that the masked class is not used within a JMRI application.
 *
 * @author Randall Wood (C) 2016, 2018
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    jmri.server.json.block.PackageTest.class,
    jmri.server.json.consist.PackageTest.class,
    jmri.server.json.idtag.PackageTest.class,
    jmri.server.json.layoutblock.PackageTest.class,
    jmri.server.json.light.PackageTest.class,
    jmri.server.json.memory.PackageTest.class,
    jmri.server.json.message.PackageTest.class,
    jmri.server.json.operations.PackageTest.class,
    jmri.server.json.power.PackageTest.class,
    jmri.server.json.reporter.PackageTest.class,
    jmri.server.json.route.PackageTest.class,
    jmri.server.json.roster.PackageTest.class,
    jmri.server.json.sensor.PackageTest.class,
    jmri.server.json.schema.PackageTest.class,
    jmri.server.json.signalhead.PackageTest.class,
    jmri.server.json.signalmast.PackageTest.class,
    jmri.server.json.time.PackageTest.class,
    jmri.server.json.turnout.PackageTest.class,
    jmri.server.json.throttle.PackageTest.class,
    jmri.server.json.util.PackageTest.class,
    JSONTest.class,
    JsonClientHandlerTest.class,
    JsonConnectionTest.class,
    JsonExceptionTest.class,
    JsonHttpServiceTest.class,
    JsonNamedBeanHttpServiceTest.class,
    JsonSocketServiceTest.class,
    JsonWebSocketTest.class
})
public class PackageTest {
}
