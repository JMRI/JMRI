package jmri.jmrix.direct;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the TrafficController class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class TrafficControllerTest {

    private TrafficController tc = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",tc);
    }

    @Before
    public void setUp(){
       JUnitUtil.setUp();
       tc = new TrafficController();
    }

    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }

}
