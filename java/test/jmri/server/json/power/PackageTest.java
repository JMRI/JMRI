package jmri.server.json.power;

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
    JsonPowerHttpServiceTest.class,
    JsonPowerSocketServiceTest.class
})
public class PackageTest {
}
