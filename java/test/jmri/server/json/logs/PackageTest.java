package jmri.server.json.logs;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Randall Wood 2017
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    JsonLogsSocketServiceTest.class,
    JsonLogsServiceFactoryTest.class,
    JsonLogsTest.class
})
public class PackageTest {
}
