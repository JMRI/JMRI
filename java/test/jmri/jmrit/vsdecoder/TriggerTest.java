package jmri.jmrit.vsdecoder;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Trigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 * @version $Revision: 17977 $
 */
public class TriggerTest extends TestCase {

    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using BoolTrigger as test vehicle.
    public void testCreate() {
        Trigger uut = new BoolTrigger("unitUnderTest");
        Assert.assertEquals("trigger name", "unitUnderTest", uut.getName());
        Assert.assertEquals("event name", "", uut.getEventName());
        Assert.assertNull("target", uut.getTarget());
        Assert.assertEquals("target action", Trigger.TargetAction.NOTHING,
                uut.getTargetAction());
        Assert.assertEquals("trigger type", Trigger.TriggerType.BOOLEAN,
                uut.getTriggerType());
    }

    public void TestSetGet() {
        VSDSound target;
        Trigger uut = new BoolTrigger("unitUnderTest");
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
            public void takeAction() {
            }

            public void takeAction(int i) {
            }

            public void takeAction(float f) {
            }
        };
        uut.setCallback(tl);
        Assert.assertSame("set callback", tl, uut.getCallback());
    }

    // from here down is testing infrastructure
    public TriggerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TriggerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TriggerTest.class);
        return suite;
    }

}
