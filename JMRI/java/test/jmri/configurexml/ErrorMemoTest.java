package jmri.configurexml;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ErrorMemoTest {

    @Test
    public void testCTor() {
        ErrorMemo t = new ErrorMemo(new jmri.jmrix.internal.configurexml.InternalSensorManagerXml(),"load","fail","IS0","",new IllegalArgumentException("test"));
        Assert.assertNotNull("exists",t);
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

    // private final static Logger log = LoggerFactory.getLogger(ErrorMemoTest.class);

}
