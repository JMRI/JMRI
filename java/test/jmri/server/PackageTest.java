package jmri.server;

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
    jmri.server.json.PackageTest.class,
    jmri.server.web.PackageTest.class
})
public class PackageTest {
}
