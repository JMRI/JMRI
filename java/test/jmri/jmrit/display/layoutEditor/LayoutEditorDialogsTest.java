package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;

/**
 * Test simple functioning of LayoutEditorDialogs
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutEditorDialogsTest {

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule    // allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries

    // testing infrastructure
//    @BeforeClass
//    public void setUpClass() {
//        JUnitUtil.setUp();
//        if (!GraphicsEnvironment.isHeadless()) {
//            JUnitUtil.resetProfileManager();
//            layoutEditor = new LayoutEditor();
//            layoutEditorDialogs = layoutEditor.getLEDialogs();
//        }
//    }
//    @AfterClass
//    public void tearDownClass() {
//        if (!GraphicsEnvironment.isHeadless()) {
//            JUnitUtil.dispose(layoutEditor);
//        }
//        layoutEditor = null;
//        layoutEditorDialogs = null;
//        JUnitUtil.tearDown();
//    }
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            layoutEditor = new LayoutEditor();
            layoutEditorDialogs = layoutEditor.getLEDialogs();
        }
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    private LayoutEditor layoutEditor = null;
    private LayoutEditorDialogs layoutEditorDialogs = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", layoutEditorDialogs);
    }

    @Test
    public void testEnterGridSizes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        layoutEditorDialogs.enterGridSizes();
    }

    @Test
    public void testEnterReporter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        layoutEditorDialogs.enterReporter(100, 100);
    }

    @Test
    public void testScaleTrackDiagram() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        layoutEditorDialogs.scaleTrackDiagram();
    }

    @Test
    public void testMoveSelection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        layoutEditorDialogs.moveSelection();
    }
}
