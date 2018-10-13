package jmri.jmrit.entryexit;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class AddEntryExitPairActionTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        AddEntryExitPairAction t = new AddEntryExitPairAction("Test Action",e);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(e);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(AddEntryExitPairActionTest.class);

}
