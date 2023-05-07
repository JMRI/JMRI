package jmri.jmrit.ctc;

import org.junit.jupiter.api.*;

/*
* Test for the CTCConstants Class
* Provide minimal coverage, there is no executable code in the class.
* @author  Dave Sand   Copyright (C) 2019
*/
public class CTCConstantsTest {

    // no testCtor as tested class only supplies static methods

    @Test
    public void testConstants() {
        Assertions.assertEquals(0, CTCConstants.LEFTTRAFFIC );
        Assertions.assertEquals(2, CTCConstants.RIGHTTRAFFIC );
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
