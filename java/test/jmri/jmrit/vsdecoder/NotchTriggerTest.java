// NotchTriggerTest.java
package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Element;

/**
 * Tests for the NotchTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class NotchTriggerTest extends TestCase {

    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using BoolTrigger as test vehicle.
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
            public void takeAction() {
            }

            public void takeAction(int i) {
            }

            public void takeAction(float f) {
            }
        };
        uut.setCallback(tl);
        Assert.assertSame("set callback", tl, uut.getCallback());
        uut.setNotch(3);
        Assert.assertEquals("match value", 3, uut.getNotch());
    }

    public void testPropertyChange() {
        NotchTrigger uut = new NotchTrigger("unitUnderTest", 4, 5);
        uut.setEventName("test event");
        uut.setCallback(new TriggerListener() {
            public void takeAction() {
                Assert.fail("wrong callback called");
            }

            public void takeAction(int i) {
                Assert.assertTrue("callback called", true);
            }

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

    // from here down is testing infrastructure
    public NotchTriggerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NotchTriggerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NotchTriggerTest.class);
        return suite;
    }

}
