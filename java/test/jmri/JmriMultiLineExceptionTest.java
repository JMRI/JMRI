package jmri;

import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test JmriMultiLineException
 * 
 * @author Daniel Bergqvist 2021
 */
public class JmriMultiLineExceptionTest {

    @Test
    public void testCtor() {
        List<String> list = new ArrayList<>();
        list.add("First row");
        list.add("Second row");
        list.add("Third row");
        list.add("Forth row");
        JmriMultiLineException obj = new JmriMultiLineException("The error", list);
        Assert.assertNotNull(obj);
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
