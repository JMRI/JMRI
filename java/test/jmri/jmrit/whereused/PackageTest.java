package jmri.jmrit.whereused;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrit.whereused package
 *
 * @author Dave Sand Copyright (C) 2020
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    SensorWhereUsedTest.class,
    WhereUsedActionTest.class,
    WhereUsedFrameTest.class
})
public class PackageTest{
}
