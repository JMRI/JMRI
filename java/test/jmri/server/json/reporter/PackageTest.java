package jmri.server.json.reporter;

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
    JsonReporterHttpServiceTest.class,
    JsonReporterSocketServiceTest.class,
    JsonReporterTest.class
})
public class PackageTest {
}
