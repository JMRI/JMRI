package jmri.jmrit.timetable.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrit.timetable.swing package
 *
 * @author Dave Sand Copyright (C) 2018
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    TimeTableActionTest.class,
    TimeTableFrameTest.class,
    TimeTableDisplayGraphTest.class,
    TimeTablePrintGraphTest.class,
    TimeTableStartupTest.class
})
public class PackageTest{
}