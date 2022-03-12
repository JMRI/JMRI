package jmri.jmrit.ctc;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/*
* Test for the CodeButtonSimulator Class
* @author  Dave Sand   Copyright (C) 2020
*/
public class CodeButtonSimulatorTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("CodeButtonSimulator Constructor Return", new CodeButtonSimulator());
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
