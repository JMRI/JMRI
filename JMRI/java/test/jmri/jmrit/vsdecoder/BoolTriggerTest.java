package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the BoolTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class BoolTriggerTest {

    @Test
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using BoolTrigger as test vehicle.
    @Test
    public void testCreateSimple() {
        BoolTrigger uut = new BoolTrigger("unitUnderTest");
        Assert.assertEquals("trigger name", "unitUnderTest", uut.getName());
        Assert.assertEquals("event name", "", uut.getEventName());
        Assert.assertNull("target", uut.getTarget());
        Assert.assertEquals("target action", Trigger.TargetAction.NOTHING,
                uut.getTargetAction());
        Assert.assertEquals("trigger type", Trigger.TriggerType.BOOLEAN,
                uut.getTriggerType());
        Assert.assertFalse("match value", uut.getMatchValue());
    }

    @Test
    public void testCreateFull() {
        BoolTrigger uut = new BoolTrigger("unitUnderTest", true);
        Assert.assertEquals("trigger name", "unitUnderTest", uut.getName());
        Assert.assertEquals("event name", "", uut.getEventName());
        Assert.assertNull("target", uut.getTarget());
        Assert.assertEquals("target action", Trigger.TargetAction.NOTHING,
                uut.getTargetAction());
        Assert.assertEquals("trigger type", Trigger.TriggerType.BOOLEAN,
                uut.getTriggerType());
        Assert.assertTrue("match value", uut.getMatchValue());
    }

    @Test
    public void TestSetGet() {
        VSDSound target;
        BoolTrigger uut = new BoolTrigger("unitUnderTest");
        uut.setName("new name");
        Assert.assertEquals("set name", "new name", uut.getName());
        uut.setEventName("event name");
        Assert.assertEquals("set event name", "event name", uut.getEventName());
        target = new ConfigurableSound("target");
        uut.setTarget(target);
        Assert.assertSame("set target", target, uut.getTarget());
        uut.setTargetName("target name");
        Assert.assertEquals("set target name", "target name", uut.getTargetName());
        uut.setTargetAction(Trigger.TargetAction.PLAY);
        Assert.assertEquals("set target action", Trigger.TargetAction.PLAY,
                uut.getTargetAction());
        uut.setTriggerType(Trigger.TriggerType.BOOLEAN);
        Assert.assertEquals("set trigger type", Trigger.TriggerType.BOOLEAN,
                uut.getTriggerType());
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
        Assert.assertSame("set callback", tl, uut.getCallback());
        uut.setMatchValue(true);
        Assert.assertTrue("match value", uut.getMatchValue());
    }

    @Test
    public void testPropertyChange() {
        BoolTrigger uut = new BoolTrigger("unitUnderTest", false);
        uut.setEventName("test event");
        uut.setMatchValue(true);
        uut.setCallback(new TriggerListener() {
            @Override
            public void takeAction() {
                Assert.assertTrue("callback called", true);
            }

            @Override
            public void takeAction(int i) {
                Assert.fail("wrong callback called");
            }

            @Override
            public void takeAction(float f) {
                Assert.fail("wrong callback called");
            }
        });
        PropertyChangeEvent e = new PropertyChangeEvent(this, "test event",
                Boolean.valueOf(false),
                Boolean.valueOf(true));
        uut.propertyChange(e);
    }

    private Element buildTestXML() {
        Element e = new Element("Trigger");
        e.setAttribute("name", "test_trigger");
        e.setAttribute("type", "BOOLEAN");
        e.addContent(new Element("event_name").addContent("test_event"));
        e.addContent(new Element("target_name").addContent("test_target"));
        e.addContent(new Element("match").addContent("TRUE"));
        e.addContent(new Element("action").addContent("PLAY"));
        return (e);
    }

    @Test
    @Ignore("Currently causes an NPE")
    public void testSetXML() {
        BoolTrigger uut = new BoolTrigger("fred"); // intentionally use wrong name
        Element e = buildTestXML();
        uut.setXml(e);
        Assert.assertEquals("xml name", "test_trigger", uut.getName());
        Assert.assertEquals("xml type", Trigger.TriggerType.BOOLEAN, uut.getTriggerType());
        Assert.assertEquals("xml event name", "test_event", uut.getEventName());
        Assert.assertEquals("xml target name", "test_target", uut.getTargetName());
        Assert.assertTrue("xml match value", uut.getMatchValue());
        Assert.assertEquals("xml action", Trigger.TargetAction.PLAY, uut.getTargetAction());

    }


}
