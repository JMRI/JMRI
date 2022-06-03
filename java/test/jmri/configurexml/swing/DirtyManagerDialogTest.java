package jmri.configurexml.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Dave Sand Copyright (C) 2022
 */
public class DirtyManagerDialogTest {

    @Test
    public void testCTor() {
        DirtyManagerDialog t = new DirtyManagerDialog();
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

    // private final static Logger log = LoggerFactory.getLogger(DirtyManagerDialogTest.class);

}
