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
 * Tests for the FloatTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class FloatTriggerTest {

    @Test
    @Disabled("Test requires further development")
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using FloatTrigger as test vehicle.
    @Test
    public void testCreateFull() {
        FloatTrigger uut = new FloatTrigger("unitUnderTest", 1.5f, Trigger.CompareType.EQ);
        assertEquals("unitUnderTest", uut.getName(), "trigger name");
        assertEquals("", uut.getEventName(), "event name");
        assertNull(uut.getTarget(), "target");
        assertEquals( Trigger.TargetAction.NOTHING,
                uut.getTargetAction(), "target action");
        assertEquals(Trigger.TriggerType.FLOAT,
                uut.getTriggerType(), "trigger type");
        assertEquals(1.5f, uut.getMatchValue(), 0.0, "match value");
    }

    @Test
    public void testSetGet() {
        VSDSound target;
        FloatTrigger uut = new FloatTrigger("unitUnderTest", 1.5f, Trigger.CompareType.EQ);
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
        uut.setTriggerType(Trigger.TriggerType.FLOAT);
        assertEquals(Trigger.TriggerType.FLOAT,
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
        uut.setMatchValue(2.0f);
        assertEquals(2.0f, uut.getMatchValue(), 0.0, "match value");
    }

    @Test
    public void testPropertyChange() {
        FloatTrigger uut = new FloatTrigger("unitUnderTest", 1.5f, Trigger.CompareType.EQ);
        uut.setEventName("test event");
        uut.setMatchValue(0.5f);
        uut.setCompareType(Trigger.CompareType.GT);
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
        PropertyChangeEvent e = new PropertyChangeEvent(this, "test event",
                1.0f,
                2.0f);
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.LT);
        e = new PropertyChangeEvent(this, "test event",
                2.0f,
                1.0f);
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.GTE);
        e = new PropertyChangeEvent(this, "test event",
                2.0f,
                0.5f);
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.LTE);
        e = new PropertyChangeEvent(this, "test event",
                2.0f,
                0.5f);
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.EQ);
        e = new PropertyChangeEvent(this, "test event",
                2.0f,
                0.5f);
        uut.propertyChange(e);
    }

    private Element buildTestXML() {
        Element e = new Element("Trigger");
        e.setAttribute("name", "test_trigger");
        e.setAttribute("type", "FLOAT");
        e.addContent(new Element("event-name").addContent("test_event"));
        e.addContent(new Element("target-name").addContent("test_target"));
        e.addContent(new Element("compare-type").addContent("GT"));
        e.addContent(new Element("match").addContent("0.5"));
        e.addContent(new Element("action").addContent("PLAY"));
        return e;
    }

    @Test
    public void testSetXML() {
        FloatTrigger uut = new FloatTrigger("unitUnderTest", 1.5f, Trigger.CompareType.EQ);
        Element e = buildTestXML();
        uut.setXml(e);
        assertEquals("test_trigger", uut.getName(), "xml name");
        assertEquals(Trigger.TriggerType.FLOAT, uut.getTriggerType(), "xml type");
        assertEquals("test_event", uut.getEventName(), "xml event name");
        assertEquals("test_target", uut.getTargetName(), "xml target name");
        assertEquals(Trigger.CompareType.GT, uut.getCompareType(), "xml compare type");
        assertEquals(0.5f, uut.getMatchValue(), 0.0, "xml match value");
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
