package jmri.jmrit.catalog;

import java.awt.GraphicsEnvironment;
import java.awt.datatransfer.DataFlavor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DragJLabelTest.class);

}
