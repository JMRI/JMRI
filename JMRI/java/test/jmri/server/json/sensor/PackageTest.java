package jmri.server.json.sensor;

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
    JsonSensorHttpServiceTest.class,
    JsonSensorSocketServiceTest.class,
    JsonSensorTest.class
})
public class PackageTest {
}
