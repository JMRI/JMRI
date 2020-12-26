package jmri.jmrit;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LogixLoadActionTest {

    @Test
    public void testCTor() {
        LogixLoadAction t = new LogixLoadAction("Test",new javax.swing.JPanel());
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LogixLoadActionTest.class);

}
