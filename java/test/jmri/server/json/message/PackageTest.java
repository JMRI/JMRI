package jmri.server.json.message;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 *
 * @author Randall Wood (C) 2017
 */
@RunWith(Suite.class)
@SuiteClasses({
    BundleTest.class,
    JsonMessageTest.class,
    JsonMessageClientManagerTest.class,
    JsonMessageHttpServiceTest.class,
    JsonMessageSocketServiceTest.class
})
public class PackageTest {
}
