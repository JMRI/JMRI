package jmri.jmrit.ctc;

import jmri.util.JUnitAppender;
import org.junit.*;

/*
* Tests for the CTCException Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class CTCExceptionTest {

    @Test
    public void testCtor() {
        CTCException ctcex = new CTCException("Test", "ID", "Param", "Reason");
        ctcex.logWarning();
        Assert.assertNotNull("CTCException Constructor Return", ctcex);

        JUnitAppender.suppressWarnMessage("Test, IDParam, Reason");
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}