package jmri.server.json.layoutblock;

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
    JsonLayoutBlockServiceFactoryTest.class,
    JsonLayoutBlockTest.class,
    JsonLayoutBlockSocketServiceTest.class,
    JsonLayoutBlockHttpServiceTest.class
})
public class PackageTest {
}
