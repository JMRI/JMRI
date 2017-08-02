package jmri.server.json.signalHead;

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
    JsonSignalHeadServiceFactoryTest.class
})
public class PackageTest {
}
