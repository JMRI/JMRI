package jmri.jmrix.loconet.Intellibox;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IntelliboxAdapterTest {

    @Test
    public void testCTor() {
        IntelliboxAdapter t = new IntelliboxAdapter();
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

    // private final static Logger log = LoggerFactory.getLogger(IntelliboxAdapterTest.class);

}
