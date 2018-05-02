package jmri.server.json.light;

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
    JsonLightHttpServiceTest.class,
    JsonLightSocketServiceTest.class,
    JsonLightTest.class
})
public class PackageTest {
}
