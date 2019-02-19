package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.netbeans.jemmy.EventTool;

/**
 * Tests for the CtcEditorAction Class
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcEditorActionTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcEditorAction();
// new EventTool().waitNoEvent(1000);
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcEditorAction().actionPerformed(null);
//         new CtcEditorAction().actionPerformed(null);
// new EventTool().waitNoEvent(1000);
    }

    @Test
    public void testMakePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        thrown.expect(IllegalArgumentException.class);
        new CtcEditorAction().makePanel();
// new EventTool().waitNoEvent(1000);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}