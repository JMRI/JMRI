package jmri.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Dave Sand Copyright (C) 2022
 */
public class StoreAndCompareTest {

    @Test
    public void testCTor() {
        StoreAndCompare sc = new StoreAndCompare();
        Assert.assertNotNull("exists", sc);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(StoreAndCompareTest.class);

}
