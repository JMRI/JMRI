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
    JsonLayoutBlockSocketServiceTest.class,
    JsonLayoutBlockTest.class
})
public class PackageTest {
}
