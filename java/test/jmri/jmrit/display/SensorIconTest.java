package jmri.jmrit.display;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of SensorIcon
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SensorIconTest extends PositionableIconTest {

    @Test
    @Override
    @DisabledIfHeadless
    public void testCtor() {
        Assertions.assertNotNull( p, "SensorIcon Constructor");
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // creates editor
        SensorIcon si = new SensorIcon(editor);
        jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        si.setSensor(new jmri.NamedBeanHandle<>("IS1", s));
        p = si;
    }

}
