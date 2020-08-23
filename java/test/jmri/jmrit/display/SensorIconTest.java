package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 * Test simple functioning of SensorIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SensorIconTest extends PositionableIconTest {

    @Test
    @Override
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("SensorIcon Constructor", p);
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            editor = new EditorScaffold();
            SensorIcon si = new SensorIcon(editor);
            jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
            si.setSensor(new jmri.NamedBeanHandle<>("IS1", s));
            p = si;
        }
    }

}
