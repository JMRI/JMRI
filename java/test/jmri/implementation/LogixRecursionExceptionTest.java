package jmri.implementation;

import static org.hamcrest.core.StringContains.containsString;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitUtil;

import static org.junit.Assert.*;

/**
 * @author Balazs Racz (C) 2018
 */
public class LogixRecursionExceptionTest {
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    @Test
    public void testCtor() throws Exception {
        Object o = new Object();
        LogixRecursionException e = new LogixRecursionException(o, "asdfgh");
        assertSame(o, e.getTriggerSource());
        assertThat(e.toString(), containsString("asdfgh"));
    }

    @Test
    public void prependDescription() throws Exception {
        Object o = new Object();
        LogixRecursionException e = new LogixRecursionException(o, "asdfgh");
        e.prependDescription("qwer");
        assertThat(e.toString(), containsString("qwer"));
    }
}