package jmri.jmrit.ctc.ctcserialdata;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Dummy test for the TrafficLockingEntry Class
* Provide minimal coverage, there is no executable code in the class.
* @author  Dave Sand   Copyright (C) 2020
*/
public class TrafficLockingEntryTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("TrafficLockingEntry Constructor Return", new TrafficLockingEntry());
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
