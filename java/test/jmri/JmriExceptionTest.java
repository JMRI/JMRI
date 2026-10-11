package jmri;

import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for JmriException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/
public class JmriExceptionTest {

    @Test
    public void testConstructor(){
        assertNotNull( new JmriException(), "JmriException constructor");
    }

    @Test
    public void testStringConstructor(){
        assertNotNull( new JmriException("test exception"), "JmriException string constructor");
    }

    @Test
    public void testArrayConstructor() {
        List<String> list = new ArrayList<>();
        list.add("First row");
        list.add("Second row");
        list.add("Third row");
        list.add("Forth row");
        JmriException obj = new JmriException("The error", list);
        assertNotNull(obj);
    }
    
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown(){
        JUnitUtil.tearDown();
    }

}
