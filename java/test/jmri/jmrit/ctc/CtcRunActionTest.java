package jmri.jmrit.ctc;

import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Tests for the CtcRunAction Class
 * @author Dave Sand Copyright (C) 2019
 */
public class CtcRunActionTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void testCreate() {
        new CtcRunAction();
    }

    @Test
    public void testAction() {
        new CtcRunAction().actionPerformed(null);
        new CtcRunAction().actionPerformed(null);
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        jmri.jmrit.ctc.setup.CreateTestObjects.createTestObjects();
        jmri.jmrit.ctc.setup.CreateTestObjects.createTestFiles();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }
}