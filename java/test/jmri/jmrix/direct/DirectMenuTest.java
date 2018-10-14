package jmri.jmrix.direct;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class DirectMenuTest {

    @Test
    public void testCTor() {
        DirectSystemConnectionMemo memo = new DirectSystemConnectionMemo();
        DirectMenu t = new DirectMenu(memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStringCTor() {
        DirectSystemConnectionMemo memo = new DirectSystemConnectionMemo();
        DirectMenu t = new DirectMenu("test",memo);
        Assert.assertNotNull("exists",t);
        Assert.assertEquals("menu text","test",t.getText());
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

    // private final static Logger log = LoggerFactory.getLogger(DirectMenuTest.class);

}
