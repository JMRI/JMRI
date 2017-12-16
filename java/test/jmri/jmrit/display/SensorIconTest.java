package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SensorIcon
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SensorIconTest extends PositionableIconTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("SensorIcon Constructor",p);
    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
           Editor ef = new EditorScaffold();
           SensorIcon si = new SensorIcon(ef);
           jmri.Sensor s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
           si.setSensor(new jmri.NamedBeanHandle<>("IS1", s));
           p=si;
        }
    }

}
