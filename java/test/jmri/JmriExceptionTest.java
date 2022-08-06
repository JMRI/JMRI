package jmri;

import java.util.ArrayList;
import java.util.List;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * Tests for JmriException class.
 *
 * @author Paul Bender Copyright (C) 2016
 **/

public class JmriExceptionTest {

   @Test
   public void testConstructor(){
      Assert.assertNotNull("JmriException constructor",new JmriException());
   }

   @Test
   public void testStringConstructor(){
      Assert.assertNotNull("JmriException string constructor",new JmriException("test exception"));
   }

    @Test
    public void testArrayConstructor() {
        List<String> list = new ArrayList<>();
        list.add("First row");
        list.add("Second row");
        list.add("Third row");
        list.add("Forth row");
        JmriException obj = new JmriException("The error", list);
        Assert.assertNotNull(obj);
    }
    
   @BeforeEach
   public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
   }

   @AfterEach
   public void tearDown(){
        JUnitUtil.tearDown();
   }

}
