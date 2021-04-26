package jmri.jmrit.ctc;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Test for the CTCConstants Class
* Provide minimal coverage, there is no executable code in the class.
* @author  Dave Sand   Copyright (C) 2019
*/
public class CTCConstantsTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("CTCConstants Constructor Return", new CTCConstants());
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
