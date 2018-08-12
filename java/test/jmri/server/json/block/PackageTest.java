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
    JsonBlockHttpServiceTest.class,
    JsonBlockSocketServiceTest.class,
    JsonBlockTest.class
})
public class PackageTest {
}
