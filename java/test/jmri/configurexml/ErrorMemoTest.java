package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ErrorMemoTest.class);

}
