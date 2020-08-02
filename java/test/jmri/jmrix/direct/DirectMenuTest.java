package jmri.jmrix.direct;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DirectMenuTest.class);

}
