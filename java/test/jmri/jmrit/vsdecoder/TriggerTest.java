package jmri.jmrit.vsdecoder;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Trigger class
 *
 * @author Mark Underwood Copyright (C) 2011
 */
public class TriggerTest {

    @Test
    public void testStateConstants() {
        // Maybe check the enums here?
    }

    // Note: Trigger is abstract.  Using BoolTrigger as test vehicle.
    @Test
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

    @Test
    public void testSetGet() {
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
    }

}
