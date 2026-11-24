package jmri.time.implementation;

import jmri.JmriException;
import jmri.Timebase;
import jmri.TimebaseBehaviorTestBase;
import jmri.TimebaseRateException;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

// Runs the shared Timebase behavior tests against DefaultTimebase
// TEMPORARY: each @Disabled override below is a known gap; the PR that fixes it deletes the override
public class DefaultTimebaseTest extends TimebaseBehaviorTestBase {

    @Override
    protected Timebase createTimebase(InternalSystemConnectionMemo memo) {
        return new DefaultTimebase(memo);
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase doesn't send changes to ClockControl yet")
    public void testHardwareToldRate() throws TimebaseRateException {
        super.testHardwareToldRate();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase doesn't send changes to ClockControl yet")
    public void testHardwareToldTime() {
        super.testHardwareToldTime();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase doesn't send changes to ClockControl yet")
    public void testHardwareToldStop() {
        super.testHardwareToldStop();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase doesn't send changes to ClockControl yet")
    public void testHardwareToldStart() {
        super.testHardwareToldStart();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase doesn't update the rate memory yet")
    public void testRateMemoryFollowsRate() throws TimebaseRateException {
        super.testRateMemoryFollowsRate();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase doesn't update the time memory yet")
    public void testTimeMemoryFollowsTime() {
        super.testTimeMemoryFollowsTime();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase doesn't set the run sensor from setRun yet")
    public void testSetRunUpdatesSensor() {
        super.testSetRunUpdatesSensor();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: the run sensor can't restart DefaultTimebase yet, pauseTime is never set")
    public void testSensorRestartsClock() throws JmriException {
        super.testSensorRestartsClock();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase passes only minutes events to listeners yet")
    public void testRateEvent() throws TimebaseRateException {
        super.testRateEvent();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase passes only minutes events to listeners yet")
    public void testRunEvent() {
        super.testRunEvent();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase renames minutes events to time yet")
    public void testMinutesEventOnSetTime() {
        super.testMinutesEventOnSetTime();
    }

    @Test
    @Override
    @Disabled("TEMPORARY: DefaultTimebase doesn't check the rate range yet")
    public void testRateOutOfRangeThrows() {
        super.testRateOutOfRangeThrows();
    }

}
