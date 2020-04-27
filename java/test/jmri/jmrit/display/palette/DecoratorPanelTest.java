package jmri.jmrit.display.palette;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.EditorScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.PositionableLabel;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DecoratorPanelTest {

    EditorScaffold editor;
    DisplayFrame df;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        editor = new EditorScaffold("Editor");
        df = new DisplayFrame("DisplayFrame", editor);
        DecoratorPanel dec = new DecoratorPanel(df);
        Assert.assertNotNull("exists", dec);
        df.dispose();
    }

    @Test
    public void testInit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        editor = new EditorScaffold("Editor");
        df = new DisplayFrame("DisplayFrame", editor);
        DecoratorPanel dec = new DecoratorPanel(df);
        dec.initDecoratorPanel(new PositionableLabel("one", editor));
        Assert.assertNotNull("exists", dec);
        df.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoratorPanelTest.class);

}
