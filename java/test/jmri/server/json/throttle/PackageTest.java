package jmri.server.json.throttle;

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
    JsonThrottleTest.class,
    JsonThrottleHttpServiceTest.class,
    JsonThrottleManagerTest.class,
    JsonThrottleServiceFactoryTest.class,
    JsonThrottleSocketServiceTest.class
})
public class PackageTest {
}
