package jmri.jmrit.vsdecoder;

import java.beans.PropertyChangeEvent;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.Element;

/**
 * Tests for the IntTrigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class IntTriggerTest extends TestCase {

    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using BoolTrigger as test vehicle.
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
            public void takeAction() {
            }

            public void takeAction(int i) {
            }

            public void takeAction(float f) {
            }
        };
        uut.setCallback(tl);
        Assert.assertSame("set callback", tl, uut.getCallback());
        uut.setMatchValue(3);
        Assert.assertEquals("match value", 3, uut.getMatchValue());
    }

    public void testPropertyChange() {
        IntTrigger uut = new IntTrigger("unitUnderTest", 4, Trigger.CompareType.EQ);
        uut.setEventName("test event");
        uut.setMatchValue(2);
        uut.setCallback(new TriggerListener() {
            public void takeAction() {
                Assert.assertTrue("callback called", true);
            }

            public void takeAction(int i) {
                Assert.fail("wrong callback called");
            }

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

    // from here down is testing infrastructure
    public IntTriggerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {IntTriggerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(IntTriggerTest.class);
        return suite;
    }

}
