package jmri.jmrit.ctc.ctcserialdata;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Test for the CallOnEntry Class
* @author  Dave Sand   Copyright (C) 2020
*/
public class CallOnEntryTest {

    @Test
    public void testCsvSplitting() {
        String ssvString = "AW-Throat,RIGHTTRAFFIC,Flashing Red,,B-Alpha-Main,IS1:SWNI,,,,,;AW-Throat,RIGHTTRAFFIC,Flashing Red,,B-Alpha-Side,IS1:SWRI,,,,,";
        for (String csvString : ProjectsCommonSubs.getArrayListFromSSV(ssvString)) {
            CallOnEntry entry = new CallOnEntry(csvString);
            Assert.assertTrue(entry._mExternalSignal.equals("AW-Throat"));
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
