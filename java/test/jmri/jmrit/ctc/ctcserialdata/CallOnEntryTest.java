package jmri.jmrit.ctc.ctcserialdata;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Dummy test for the CallOnEntry Class
* Provide minimal coverage, there is no executable code in the class.
* @author  Dave Sand   Copyright (C) 2020
*/
public class CallOnEntryTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("CallOnEntry Constructor Return", new CallOnEntry(""));
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
