package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of MultiSensorIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class MultiSensorIconTest extends PositionableTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("MultiSensorIcon Constructor", p);
    }

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            p = new MultiSensorIcon(editor);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
