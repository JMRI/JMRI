package jmri.server.json.time;

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
    JsonTimeServiceFactoryTest.class,
    JsonTimeSocketServiceTest.class,
    JsonTimeHttpServiceTest.class
})
public class PackageTest {
}
