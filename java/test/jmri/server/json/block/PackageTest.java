package jmri.server.json.block;

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
    JsonBlockServiceFactoryTest.class,
    JsonBlockTest.class,
    JsonBlockSocketServiceTest.class,
    JsonBlockHttpServiceTest.class
})
public class PackageTest {
}
