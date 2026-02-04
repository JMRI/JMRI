package jmri.jmrit.vsdecoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;

import org.jdom2.Element;

import org.junit.jupiter.api.*;

/**
 * Tests for the ButtonTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class ButtonTriggerTest {

    @Test
    @Disabled("Test requires further development")
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using ButtonTrigger as test vehicle.
    @Test
    public void testCreateSimple() {
        ButtonTrigger uut = new ButtonTrigger("unitUnderTest");
        assertEquals("unitUnderTest", uut.getName(), "trigger name");
        assertEquals("", uut.getEventName(), "event name");
        assertNull(uut.getTarget(), "target");
        assertEquals(Trigger.TargetAction.NOTHING,
                uut.getTargetAction(), "target action");
        assertEquals(Trigger.TriggerType.BUTTON,
                uut.getTriggerType(), "trigger type");
        assertFalse(uut.getMatchValue(), "match value");
    }

    @Test
    public void testCreateFull() {
        ButtonTrigger uut = new ButtonTrigger("unitUnderTest", true);
        assertEquals("unitUnderTest", uut.getName(), "trigger name");
        assertEquals("", uut.getEventName(), "event name");
        assertNull(uut.getTarget(), "target");
        assertEquals(Trigger.TargetAction.NOTHING,
                uut.getTargetAction(), "target action");
        assertEquals(Trigger.TriggerType.BUTTON,
                uut.getTriggerType(), "trigger type");
        assertTrue(uut.getMatchValue(), "match value");
    }

    @Test
    public void testButtonTiggerSetGet() {
        VSDSound target;
        ButtonTrigger uut = new ButtonTrigger("unitUnderTest");
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
        uut.setTriggerType(Trigger.TriggerType.BUTTON);
        assertEquals(Trigger.TriggerType.BUTTON,
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
        uut.setMatchValue(true);
        assertTrue(uut.getMatchValue(), "match value");
    }

    @Test
    public void testPropertyChange() {
        ButtonTrigger uut = new ButtonTrigger("unitUnderTest", false);
        uut.setEventName("test event");
        uut.setMatchValue(true);
        uut.setCallback(new TriggerListener() {
            @Override
            public void takeAction() {
                assertTrue(true, "callback called");
            }

            @Override
            public void takeAction(int i) {
                fail("wrong callback called");
            }

            @Override
            public void takeAction(float f) {
                fail("wrong callback called");
            }
        });
        PropertyChangeEvent e = new PropertyChangeEvent(this, "test event", false, true);
        uut.propertyChange(e);
    }

    private Element buildTestXML() {
        Element e = new Element("Trigger");
        e.setAttribute("name", "test_trigger");
        e.setAttribute("type", "BUTTON");
        e.addContent(new Element("event-name").addContent("test_event"));
        e.addContent(new Element("target-name").addContent("test_target"));
        e.addContent(new Element("match").addContent("TRUE"));
        e.addContent(new Element("action").addContent("PLAY"));
        return e;
    }

    @Test
    public void testSetXML() {
        ButtonTrigger uut = new ButtonTrigger("fred"); // intentionally use wrong name
        Element e = buildTestXML();
        uut.setXml(e);
        assertEquals("test_trigger", uut.getName(), "xml name");
        assertEquals(Trigger.TriggerType.BUTTON, uut.getTriggerType(), "xml type");
        assertEquals("test_event", uut.getEventName(), "xml event name");
        assertEquals("test_target", uut.getTargetName(), "xml target name");
        assertTrue(uut.getMatchValue(), "xml match value");
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
