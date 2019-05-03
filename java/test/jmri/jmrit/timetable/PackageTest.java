package jmri.jmrit.timetable;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrit.timetable package
 *
 * @author Dave Sand Copyright (C) 2018
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    LayoutTest.class,
    ScheduleTest.class,
    SegmentTest.class,
    StationTest.class,
    StopTest.class,
    TimeTableDataManagerTest.class,
    TimeTableImportTest.class,
    TimeTableCsvImportTest.class,
    TimeTableCsvExportTest.class,
    TrainTest.class,
    TrainTypeTest.class,
    jmri.jmrit.timetable.configurexml.PackageTest.class,
    jmri.jmrit.timetable.swing.PackageTest.class
})
public class PackageTest{
}