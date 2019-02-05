package jmri.jmrit.ctc;

import java.awt.GraphicsEnvironment;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Tests for the CtcRunAction Class
 * @author Dave Sand Copyright (C) 2018
 */
public class CtcRunActionTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
//         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcRunAction();
    }

    @Test
    public void testAction() {
//         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new CtcRunAction().actionPerformed(null);
        new CtcRunAction().actionPerformed(null);
    }

    @Test
    public void testMakePanel() {
//         Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        thrown.expect(IllegalArgumentException.class);
        new CtcRunAction().makePanel();
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