package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of FunctionButtonPropertyEditor
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class FunctionButtonPropertyEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        FunctionButtonPropertyEditor dialog = new FunctionButtonPropertyEditor();
        Assert.assertNotNull("exists", dialog);
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
    }
}
