package jmri.jmrit.display;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;

import jmri.JmriException;
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

        x=2;
        event = new JmriMouseEvent(source, id, when, modifiers, x, y+2, 
                                            clickCount, popupTrigger);
        p.doMouseReleased(event);
        
        Assertions.assertEquals(Sensor.INACTIVE, s.getCommandedState());
    }

    @Test
    @DisabledIfHeadless
    public void testStateChangeRepaintsBoundedArea() throws JmriException {
        si.setBounds(12, 17, 20, 30);
        EditorScaffold scaffold = (EditorScaffold) editor;
        scaffold.lastRepaintRect = null;

        s.setKnownState(Sensor.ACTIVE);

        Assertions.assertNotNull( scaffold.lastRepaintRect,
                "state change posts a bounded repaint instead of repainting the frame");
        Assertions.assertTrue( scaffold.lastRepaintRect.contains(new Rectangle(12, 17, 20, 30)),
                "dirty area covers the icon bounds");
    }

    @Test
    @DisabledIfHeadless
    public void testTextStateChangeIsNotADisplayOptionChange() throws JmriException {
        int[] notified = {0};
        Editor counting = new EditorScaffold() {
            @Override
            public void notifyWebPanelDisplayOptionsChanged() {
                notified[0]++;
            }
        };
        SensorIcon text = new SensorIcon("  ", counting); // as SensorIconXml makes a text icon
        text.setSensor(new jmri.NamedBeanHandle<>("IS1", s));
        Assertions.assertTrue(text.isText());
        notified[0] = 0;

        s.setKnownState(Sensor.ACTIVE);
        s.setKnownState(Sensor.INACTIVE);
        Assertions.assertEquals(Color.yellow, text.getForeground(), "the state's colors are shown");
        Assertions.assertEquals(0, notified[0], "a state change doesn't make web clients load the panel again");

        text.setTextInActive(Color.green);
        Assertions.assertEquals(Color.green, text.getForeground());
        Assertions.assertEquals(1, notified[0], "editing a state's color still tells web clients");
        text.setTextActive(Color.green);
        text.setTextUnknown(Color.green);
        text.setTextInconsistent(Color.green);
        text.setBackgroundActive(Color.green);
        text.setBackgroundInActive(Color.green);
        text.setBackgroundUnknown(Color.green);
        text.setBackgroundInconsistent(Color.green);
        Assertions.assertEquals(8, notified[0], "once for each edit");

        text.dispose();
        jmri.util.JUnitUtil.dispose(counting);
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
