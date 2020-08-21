package jmri.jmrit.ctc;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Tests for the SignalDirectionIndicatorsNull Class
* @author  Dave Sand   Copyright (C) 2019
*/
public class SignalDirectionIndicatorsNullTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("SignalDirectionIndicatorsNull Constructor Return", new SignalDirectionIndicatorsNull());
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
