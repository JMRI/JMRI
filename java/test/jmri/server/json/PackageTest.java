package jmri.server.json;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.runner.RunWith;

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
@RunWith(JUnitPlatform.class)
@SelectPackages("jmri.server.json")
public class PackageTest {
}
