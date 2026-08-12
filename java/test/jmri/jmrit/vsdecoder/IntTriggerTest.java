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
 * Tests for the IntTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class IntTriggerTest {

    @Test
    @Disabled("Test requires further development")
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using IntTrigger as test vehicle.
    @Test
    public void testCreateSimple() {
        IntTrigger uut = new IntTrigger("unitUnderTest");
        assertEquals("unitUnderTest", uut.getName(), "trigger name");
        assertEquals("", uut.getEventName(), "event name");
        assertNull(uut.getTarget(), "target");
        assertEquals(Trigger.TargetAction.NOTHING,
                uut.getTargetAction(), "target action");
        assertEquals(Trigger.TriggerType.INT,
                uut.getTriggerType(), "trigger type");
        assertEquals(0, uut.getMatchValue(), "match value");
    }

    @Test
    public void testCreateFull() {
        IntTrigger uut = new IntTrigger("unitUnderTest", 2, Trigger.CompareType.EQ);
        assertEquals("unitUnderTest", uut.getName(), "trigger name");
        assertEquals("", uut.getEventName(), "event name");
        assertNull(uut.getTarget(), "target");
        assertEquals(Trigger.TargetAction.NOTHING,
                uut.getTargetAction(), "target action");
        assertEquals(Trigger.TriggerType.INT,
                uut.getTriggerType(), "trigger type");
        assertEquals(2, uut.getMatchValue(), "match value");
    }

    @Test
    public void testSetGet() {
        VSDSound target;
        IntTrigger uut = new IntTrigger("unitUnderTest", 3, Trigger.CompareType.EQ);
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
        uut.setTriggerType(Trigger.TriggerType.INT);
        assertEquals(Trigger.TriggerType.INT, uut.getTriggerType(), "set trigger type");
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
        uut.setMatchValue(3);
        assertEquals(3, uut.getMatchValue(), "match value");
    }

    @Test
    public void testPropertyChange() {
        IntTrigger uut = new IntTrigger("unitUnderTest", 4, Trigger.CompareType.EQ);
        uut.setEventName("test event");
        uut.setMatchValue(2);
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
        uut.setCompareType(Trigger.CompareType.GT);
        PropertyChangeEvent e = new PropertyChangeEvent(this, "test event", 1, 3);
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.LT);
        e = new PropertyChangeEvent(this, "test event", 3, 1);
        //new Integer(3), 
        //new Integer(1));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.GTE);
        e = new PropertyChangeEvent(this, "test event", 1, 2);
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.LTE);
        e = new PropertyChangeEvent(this, "test event", 3, 2);
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.EQ);
        e = new PropertyChangeEvent(this, "test event", 3, 2);
        uut.propertyChange(e);
    }

    private Element buildTestXML() {
        Element e = new Element("Trigger");
        e.setAttribute("name", "test_trigger");
        e.setAttribute("type", "INT");
        e.addContent(new Element("event-name").addContent("test_event"));
        e.addContent(new Element("target-name").addContent("test_target"));
        e.addContent(new Element("compare-type").addContent("GT"));
        e.addContent(new Element("match").addContent("2"));
        e.addContent(new Element("action").addContent("PLAY"));
        return e;
    }

    @Test
    public void testSetXML() {
        IntTrigger uut = new IntTrigger("unitUnderTest", 3, Trigger.CompareType.EQ);
        Element e = buildTestXML();
        uut.setXml(e);
        assertEquals("test_trigger", uut.getName(), "xml name");
        assertEquals(Trigger.TriggerType.INT, uut.getTriggerType(), "xml type");
        assertEquals("test_event", uut.getEventName(), "xml event name");
        assertEquals("test_target", uut.getTargetName(), "xml target name");
        assertEquals(Trigger.CompareType.GT, uut.getCompareType(), "xml compare type");
        assertEquals(2, uut.getMatchValue(), "xml match value");
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
