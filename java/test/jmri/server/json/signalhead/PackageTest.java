package jmri.server.json.signalhead;

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
    JsonSignalHeadHttpServiceTest.class,
    JsonSignalHeadSocketServiceTest.class,
    JsonSignalHeadTest.class
})
public class PackageTest {
}
