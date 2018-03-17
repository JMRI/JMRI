package jmri.server.json.schema;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Randall Wood Copyright 2018
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    JsonSchemaServiceCacheTest.class,
    JsonSchemaHttpServiceTest.class,
    JsonSchemaSocketServiceTest.class
})
public class PackageTest {
}
