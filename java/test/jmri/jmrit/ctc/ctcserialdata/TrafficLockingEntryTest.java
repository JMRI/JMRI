package jmri.jmrit.ctc.ctcserialdata;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Dummy test for the TrafficLockingEntry Class
* @author  Dave Sand   Copyright (C) 2020
*/
public class TrafficLockingEntryTest {

    @Test
    public void testCsvSplitting() {
        String ssvString = "Rule #:1,Enabled,EB-West,,1/2,Normal,,Normal,,Normal,,Normal,,Normal,S-AW,S-AW-Approach,,,,,,,,,,,0,,,,; Rule #:2,Enabled,EB-West,,1/2,Reverse,,Normal,,Normal,,Normal,,Normal,S-AW,S-AW-Approach,,,,,,,,,,,0,,,,";
        for (String csvString : ProjectsCommonSubs.getArrayListFromSSV(ssvString)) {
            TrafficLockingEntry entry = new TrafficLockingEntry(csvString);
            Assert.assertTrue(entry._mDestinationSignalOrComment.equals("EB-West"));
        }
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}
