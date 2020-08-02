package jmri.jmrit.catalog;

import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.DataFlavor;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DragJLabelTest {

    @Test
    public void testCTor() throws java.lang.ClassNotFoundException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        DragJLabel t = new DragJLabel(DataFlavor.stringFlavor);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DragJLabelTest.class);

}
