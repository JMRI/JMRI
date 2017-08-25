package jmri.server.json.consist;

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
    JsonConsistManagerTest.class,
    JsonConsistServiceFactoryTest.class,
    JsonConsistTest.class,
    JsonConsistSocketServiceTest.class,
    JsonConsistHttpServiceTest.class
})
public class PackageTest {
}
