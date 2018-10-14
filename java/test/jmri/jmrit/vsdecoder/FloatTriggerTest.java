package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import org.jdom2.Element;
import org.junit.*;

/**
 * Tests for the FloatTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class FloatTriggerTest {

    @Test
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using BoolTrigger as test vehicle.
    @Test
    public void testCreateFull() {
        FloatTrigger uut = new FloatTrigger("unitUnderTest", 1.5f, Trigger.CompareType.EQ);
        Assert.assertEquals("trigger name", "unitUnderTest", uut.getName());
        Assert.assertEquals("event name", "", uut.getEventName());
        Assert.assertNull("target", uut.getTarget());
        Assert.assertEquals("target action", Trigger.TargetAction.NOTHING,
                uut.getTargetAction());
        Assert.assertEquals("trigger type", Trigger.TriggerType.FLOAT,
                uut.getTriggerType());
        Assert.assertEquals("match value", 1.5f, uut.getMatchValue(), 0.0);
    }

    @Test
    public void TestSetGet() {
        VSDSound target;
        FloatTrigger uut = new FloatTrigger("unitUnderTest", 1.5f, Trigger.CompareType.EQ);
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
        uut.setTriggerType(Trigger.TriggerType.FLOAT);
        Assert.assertEquals("set trigger type", Trigger.TriggerType.FLOAT,
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
        uut.setMatchValue(2.0f);
        Assert.assertEquals("match value", 2.0f, uut.getMatchValue(), 0.0);
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
                new Float(1.0f),
                new Float(2.0f));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.LT);
        e = new PropertyChangeEvent(this, "test event",
                new Float(2.0f),
                new Float(1.0f));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.GTE);
        e = new PropertyChangeEvent(this, "test event",
                new Float(2.0f),
                new Float(0.5f));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.LTE);
        e = new PropertyChangeEvent(this, "test event",
                new Float(2.0f),
                new Float(0.5f));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.EQ);
        e = new PropertyChangeEvent(this, "test event",
                new Float(2.0f),
                new Float(0.5f));
        uut.propertyChange(e);
    }

    private Element buildTestXML() {
        Element e = new Element("Trigger");
        e.setAttribute("name", "test_trigger");
        e.setAttribute("type", "FLOAT");
        e.addContent(new Element("event_name").addContent("test_event"));
        e.addContent(new Element("target_name").addContent("test_target"));
        e.addContent(new Element("compare_type").addContent("GT"));
        e.addContent(new Element("match").addContent("0.5"));
        e.addContent(new Element("action").addContent("PLAY"));
        return (e);
    }

    @Test
    @Ignore("Causes NPE")
    public void testSetXML() {
        FloatTrigger uut = new FloatTrigger("unitUnderTest", 1.5f, Trigger.CompareType.EQ);
        Element e = buildTestXML();
        uut.setXml(e);
        Assert.assertEquals("xml name", "test_trigger", uut.getName());
        Assert.assertEquals("xml type", Trigger.TriggerType.FLOAT, uut.getTriggerType());
        Assert.assertEquals("xml event name", "test_event", uut.getEventName());
        Assert.assertEquals("xml target name", "test_target", uut.getTargetName());
        Assert.assertEquals("xml compare type", Trigger.CompareType.GT, uut.getCompareType());
        Assert.assertEquals("xml match value", 0.5f, uut.getMatchValue(), 0.0);
        Assert.assertEquals("xml action", Trigger.TargetAction.PLAY, uut.getTargetAction());

    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
