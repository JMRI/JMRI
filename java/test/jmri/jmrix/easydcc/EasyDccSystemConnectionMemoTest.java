package jmri.jmrix.easydcc;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the EasyDccSystemConnectionMemo class
 * <p>
 *
 * @author      Paul Bender Copyright (C) 2016
 */
public class EasyDccSystemConnectionMemoTest {

    private EasyDccSystemConnectionMemo memo = null;

    @Test
    public void testCtor(){
       Assert.assertNotNull("exists",memo); 
    }

    @Before
    public void setUp(){
       JUnitUtil.setUp();
       memo = new EasyDccSystemConnectionMemo();
    }

    @After
    public void tearDown(){
       JUnitUtil.tearDown();
    }
}
