package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import org.jdom2.Element;
import org.junit.*;

/**
 * Tests for the IntTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class IntTriggerTest {

    @Test
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using BoolTrigger as test vehicle.
    @Test
    public void testCreateSimple() {
        IntTrigger uut = new IntTrigger("unitUnderTest");
        Assert.assertEquals("trigger name", "unitUnderTest", uut.getName());
        Assert.assertEquals("event name", "", uut.getEventName());
        Assert.assertNull("target", uut.getTarget());
        Assert.assertEquals("target action", Trigger.TargetAction.NOTHING,
                uut.getTargetAction());
        Assert.assertEquals("trigger type", Trigger.TriggerType.INT,
                uut.getTriggerType());
        Assert.assertEquals("match value", 0, uut.getMatchValue());
    }

    @Test
    public void testCreateFull() {
        IntTrigger uut = new IntTrigger("unitUnderTest", 2, Trigger.CompareType.EQ);
        Assert.assertEquals("trigger name", "unitUnderTest", uut.getName());
        Assert.assertEquals("event name", "", uut.getEventName());
        Assert.assertNull("target", uut.getTarget());
        Assert.assertEquals("target action", Trigger.TargetAction.NOTHING,
                uut.getTargetAction());
        Assert.assertEquals("trigger type", Trigger.TriggerType.INT,
                uut.getTriggerType());
        Assert.assertEquals("match value", 2, uut.getMatchValue());
    }

    @Test
    public void TestSetGet() {
        VSDSound target;
        IntTrigger uut = new IntTrigger("unitUnderTest", 3, Trigger.CompareType.EQ);
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
        uut.setTriggerType(Trigger.TriggerType.INT);
        Assert.assertEquals("set trigger type", Trigger.TriggerType.INT,
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
        uut.setMatchValue(3);
        Assert.assertEquals("match value", 3, uut.getMatchValue());
    }

    @Test
    public void testPropertyChange() {
        IntTrigger uut = new IntTrigger("unitUnderTest", 4, Trigger.CompareType.EQ);
        uut.setEventName("test event");
        uut.setMatchValue(2);
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
        uut.setCompareType(Trigger.CompareType.GT);
        PropertyChangeEvent e = new PropertyChangeEvent(this, "test event",
                Integer.valueOf(1),
                Integer.valueOf(3));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.LT);
        e = new PropertyChangeEvent(this, "test event", 3, 1);
        //new Integer(3), 
        //new Integer(1));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.GTE);
        e = new PropertyChangeEvent(this, "test event",
                Integer.valueOf(1),
                Integer.valueOf(2));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.LTE);
        e = new PropertyChangeEvent(this, "test event",
                Integer.valueOf(3),
                Integer.valueOf(2));
        uut.propertyChange(e);

        uut.setCompareType(Trigger.CompareType.EQ);
        e = new PropertyChangeEvent(this, "test event",
                Integer.valueOf(3),
                Integer.valueOf(2));
        uut.propertyChange(e);
    }

    private Element buildTestXML() {
        Element e = new Element("Trigger");
        e.setAttribute("name", "test_trigger");
        e.setAttribute("type", "INT");
        e.addContent(new Element("event_name").addContent("test_event"));
        e.addContent(new Element("target_name").addContent("test_target"));
        e.addContent(new Element("compare_type").addContent("GT"));
        e.addContent(new Element("match").addContent("2"));
        e.addContent(new Element("action").addContent("PLAY"));
        return (e);
    }

    @Test
    @Ignore("Causes NPE")
    public void testSetXML() {
        IntTrigger uut = new IntTrigger("unitUnderTest", 3, Trigger.CompareType.EQ);
        Element e = buildTestXML();
        uut.setXml(e);
        Assert.assertEquals("xml name", "test_trigger", uut.getName());
        Assert.assertEquals("xml type", Trigger.TriggerType.INT, uut.getTriggerType());
        Assert.assertEquals("xml event name", "test_event", uut.getEventName());
        Assert.assertEquals("xml target name", "test_target", uut.getTargetName());
        Assert.assertEquals("xml compare type", Trigger.CompareType.GT, uut.getCompareType());
        Assert.assertEquals("xml match value", 2, uut.getMatchValue());
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
