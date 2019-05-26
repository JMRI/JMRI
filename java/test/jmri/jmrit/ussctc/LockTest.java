package jmri.jmrit.ussctc;

import java.util.*;
import jmri.*;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for Lock class in the jmri.jmrit.ussctc package
 *
 * @author	Paul Bender Copyright 2018
 */
public class LockTest {

    @Test
    public void testEnumValues() {
        // this is a pretty poor test, but the class under test is an interface         
        // with an enum that isn't used anywhere in the Java code.
        Assert.assertNotEquals(Lock.Valid.FIELD_TURNOUT,Lock.Valid.FIELD_SIGNAL);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
