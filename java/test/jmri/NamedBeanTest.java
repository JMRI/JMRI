package jmri;

import org.junit.jupiter.api.*;

/**
 * Tests for the NamedBean interface
 *
 * @author Bob Jacobsen Copyright (C) 2017
 */
public class NamedBeanTest {

    // Note: This shows that BadUserNameException doesn't (yet) have to be caught or declared
    // Eventually that will go away, and that'll be OK
    @Test
    public void testNormalizePassThrough() {
        String testString = "  foo ";
        String normalForm = NamedBean.normalizeUserName(testString);
        //note: normalizeUserName now .trim()'s;
        Assertions.assertEquals("foo", normalForm);
    }

    @BeforeEach
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }
}
