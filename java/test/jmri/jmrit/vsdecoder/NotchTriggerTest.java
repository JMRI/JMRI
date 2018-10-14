package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import org.jdom2.Element;
import org.junit.*;

/**
 * Tests for the NotchTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class NotchTriggerTest {

    @Test
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using BoolTrigger as test vehicle.
    @Test
    public void testCreateSimple() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest");
        Assert.assertEquals("trigger name", "unitUnderTest", uut.getName());
        Assert.assertEquals("event name", "", uut.getEventName());
        Assert.assertNull("target", uut.getTarget());
        Assert.assertEquals("target action", Trigger.TargetAction.NOTHING,
                uut.getTargetAction());
        Assert.assertEquals("trigger type", Trigger.TriggerType.NOTCH,
                uut.getTriggerType());
        Assert.assertEquals("notch value", 0, uut.getNotch());
    }

    @Test
    public void testCreateFull() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 2, 3);
        Assert.assertEquals("trigger name", "unitUnderTest", uut.getName());
        Assert.assertEquals("event name", "", uut.getEventName());
        Assert.assertNull("target", uut.getTarget());
        Assert.assertEquals("target action", Trigger.TargetAction.NOTHING,
                uut.getTargetAction());
        Assert.assertEquals("trigger type", Trigger.TriggerType.NOTCH,
                uut.getTriggerType());
        Assert.assertEquals("notch value", 3, uut.getNotch());
    }

    @Test
    public void TestSetGet() {
        VSDSound target;
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 3, 4);
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
        uut.setTriggerType(Trigger.TriggerType.NOTCH);
        Assert.assertEquals("set trigger type", Trigger.TriggerType.NOTCH,
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
        uut.setNotch(3);
        Assert.assertEquals("match value", 3, uut.getNotch());
    }

    @Test
    public void testPropertyChange() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 4, 5);
        uut.setEventName("test event");
        uut.setCallback(new TriggerListener() {
            @Override
            public void takeAction() {
                Assert.fail("wrong callback called");
            }

            @Override
            public void takeAction(int i) {
                Assert.assertTrue("callback called", true);
            }

            @Override
            public void takeAction(float f) {
                Assert.fail("wrong callback called");
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
        e.addContent(new Element("event_name").addContent("test_event"));
        e.addContent(new Element("target_name").addContent("test_target"));
        e.addContent(new Element("match").addContent("2"));
        e.addContent(new Element("action").addContent("PLAY"));
        return (e);
    }

    @Test
    @Ignore("Causes NPE")
    public void testSetXML() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 3, 4);
        Element e = buildTestXML();
        uut.setXml(e);
        Assert.assertEquals("xml name", "test_trigger", uut.getName());
        Assert.assertEquals("xml type", Trigger.TriggerType.NOTCH, uut.getTriggerType());
        Assert.assertEquals("xml event name", "test_event", uut.getEventName());
        Assert.assertEquals("xml target name", "test_target", uut.getTargetName());
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
