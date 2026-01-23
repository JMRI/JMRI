package jmri.jmrit.display;

import java.awt.Component;

import jmri.Sensor;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.swing.JmriMouseEvent;

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

    @Test
    @DisabledIfHeadless
    public void testClickStationary() {

        Assertions.assertEquals(Sensor.UNKNOWN, s.getCommandedState());

        Component source = si;
        int id = 1001;
        long when = 50000;
        int modifiers = 0;
        int x = 3;
        int y = 3;
        int clickCount = 0;
        boolean popupTrigger = false;
        var event = new JmriMouseEvent(source, id, when, modifiers, x, y, 
                                            clickCount, popupTrigger);
        
        p.doMousePressed(event);
        p.doMouseClicked(event);
        p.doMouseReleased(event);
        
        Assertions.assertEquals(Sensor.INACTIVE, s.getCommandedState());

        p.doMousePressed(event);
        p.doMouseClicked(event);
        p.doMouseReleased(event);
        
        Assertions.assertEquals(Sensor.ACTIVE, s.getCommandedState());
    }

    @Test
    @DisabledIfHeadless
    public void testClickMove2() {

        Assertions.assertEquals(Sensor.UNKNOWN, s.getCommandedState());

        Component source = si;
        int id = 1001;
        long when = 50000;
        int modifiers = 0;
        int x = 3;
        int y = 3;
        int clickCount = 0;
        boolean popupTrigger = false;
        var event = new JmriMouseEvent(source, id, when, modifiers, x, y, 
                                            clickCount, popupTrigger);
        
        p.doMousePressed(event);
        
        event = new JmriMouseEvent(source, id, when, modifiers, x+2, y, 
                                            clickCount, popupTrigger);
        p.doMouseClicked(event);
        
        event = new JmriMouseEvent(source, id, when, modifiers, x=2, y+2, 
                                            clickCount, popupTrigger);
        p.doMouseReleased(event);
        
        Assertions.assertEquals(Sensor.INACTIVE, s.getCommandedState());
    }

    Sensor s;
    SensorIcon si;
    
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp(); // creates editor
        si = new SensorIcon(editor);
        s = jmri.InstanceManager.sensorManagerInstance().provideSensor("IS1");
        si.setSensor(new jmri.NamedBeanHandle<>("IS1", s));
        p = si;
    }

}
