package jmri.jmrit.ctc.editor;

import java.awt.GraphicsEnvironment;
import java.util.Locale;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Tests for the CtcEditorStartup Class
 * @author Dave Sand Copyright (C) 2018
 */
public class CtcEditorStartupTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcEditorStartup();
    }

    @Test
    public void testGetTitle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("Open CTC Editor", new CtcEditorStartup().getTitle(CtcEditorAction.class, Locale.US));  // NOI18N
    }

//     @Test
//     public void testGetTitleException() {
//         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
//         thrown.expect(IllegalArgumentException.class);
//         Assert.assertEquals("Open Ctc Editor Exception", new CtcEditorStartup().getTitle(CtcEditor.class, Locale.US));  // NOI18N
//     }

    @Test
    public void testGetClass() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull(new CtcEditorStartup().getActionClasses());
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