package jmri.jmrit.vsdecoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;

import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 * Tests for the NotchTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class NotchTriggerTest {

    @Test
    @Disabled("Test requires further development")
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using NotchTrigger as test vehicle.
    @Test
    public void testCreateSimple() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest");
        assertEquals("unitUnderTest", uut.getName(), "trigger name");
        assertEquals("", uut.getEventName(), "event name");
        assertNull(uut.getTarget(), "target");
        assertEquals(Trigger.TargetAction.NOTHING,
            uut.getTargetAction(), "target action");
        assertEquals(Trigger.TriggerType.NOTCH,
            uut.getTriggerType(), "trigger type");
        assertEquals(0, uut.getNotch(), "notch value");
    }

    @Test
    public void testCreateFull() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 2, 3);
        assertEquals("unitUnderTest", uut.getName(), "trigger name");
        assertEquals("", uut.getEventName(), "event name");
        assertNull(uut.getTarget(), "target");
        assertEquals(Trigger.TargetAction.NOTHING,
                uut.getTargetAction(), "target action");
        assertEquals(Trigger.TriggerType.NOTCH,
                uut.getTriggerType(), "trigger type");
        assertEquals(3, uut.getNotch(), "notch value");
    }

    @Test
    public void testSetGet() {
        VSDSound target;
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 3, 4);
        uut.setName("new name");
        assertEquals("new name", uut.getName(), "set name");
        uut.setEventName("event name");
        assertEquals("event name", uut.getEventName(), "set event name");
        target = new ConfigurableSound("target");
        uut.setTarget(target);
        assertSame(target, uut.getTarget(), "set target");
        uut.setTargetName("target name");
        assertEquals("target name", uut.getTargetName(), "set target name");
        uut.setTargetAction(Trigger.TargetAction.PLAY);
        assertEquals(Trigger.TargetAction.PLAY,
                uut.getTargetAction(), "set target action");
        uut.setTriggerType(Trigger.TriggerType.NOTCH);
        assertEquals(Trigger.TriggerType.NOTCH,
                uut.getTriggerType(), "set trigger type");
        TriggerListener tl = new TriggerListener() {
            @Override
            public void takeAction() {
            }

            @Override
            public void takeAction(int i) {
            }

            @Override
            public void takeAction(float f) {
            }
        };
        uut.setCallback(tl);
        assertSame(tl, uut.getCallback(), "set callback");
        uut.setNotch(3);
        assertEquals(3, uut.getNotch(), "match value");
    }

    @Test
    public void testPropertyChange() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 4, 5);
        uut.setEventName("test event");
        uut.setCallback(new TriggerListener() {
            @Override
            public void takeAction() {
                fail("wrong callback called");
            }

            @Override
            public void takeAction(int i) {
                assertTrue(true, "callback called");
            }

            @Override
            public void takeAction(float f) {
                fail("wrong callback called");
            }
        });
        uut.setNotch(2); // 2/8 = 0.25
        PropertyChangeEvent e = new PropertyChangeEvent(this, "test event",
                0.2f, 0.3f);
        uut.propertyChange(e);

        e = new PropertyChangeEvent(this, "test event", 0.3, 0.2);
        uut.propertyChange(e);
    }

    private Element buildTestXML() {
        Element e = new Element("Trigger");
        e.setAttribute("name", "test_trigger");
        e.setAttribute("type", "NOTCH");
        e.addContent(new Element("event-name").addContent("test_event"));
        e.addContent(new Element("target-name").addContent("test_target"));
        e.addContent(new Element("match").addContent("2"));
        e.addContent(new Element("action").addContent("PLAY"));
        return e;
    }

    @Test
    public void testSetXML() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 3, 4);
        Element e = buildTestXML();
        uut.setXml(e);
        assertEquals("test_trigger", uut.getName(), "xml name");
        assertEquals(Trigger.TriggerType.NOTCH, uut.getTriggerType(), "xml type");
        assertEquals("test_event", uut.getEventName(), "xml event name");
        assertEquals("test_target", uut.getTargetName(), "xml target name");
        assertEquals(Trigger.TargetAction.PLAY, uut.getTargetAction(), "xml action");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
