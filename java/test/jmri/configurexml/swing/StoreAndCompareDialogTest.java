package jmri.configurexml.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Dave Sand Copyright (C) 2022
 */
public class StoreAndCompareDialogTest {

    @Test
    public void testCTor() {
        StoreAndCompareDialog t = new StoreAndCompareDialog();
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

    // private final static Logger log = LoggerFactory.getLogger(StoreAndCompareDialogTest.class);

}
