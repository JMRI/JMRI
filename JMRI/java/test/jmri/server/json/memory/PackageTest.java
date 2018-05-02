package jmri.server.json.memory;

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
    JsonMemoryHttpServiceTest.class,
    JsonMemorySocketServiceTest.class,
    JsonMemoryTest.class
})
public class PackageTest {
}
