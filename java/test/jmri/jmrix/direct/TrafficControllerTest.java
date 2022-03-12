package jmri.jmrix.direct;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * JUnit tests for the TrafficController class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class TrafficControllerTest {

    private TrafficController tc = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists", tc);
    }

    @BeforeEach
    public void setUp(){
       JUnitUtil.setUp();

       DirectSystemConnectionMemo m = new DirectSystemConnectionMemo();
       tc = new TrafficController(m);
    }

    @AfterEach
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
