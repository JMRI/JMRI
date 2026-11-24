package jmri.jmrit.simpleclock;

import jmri.Timebase;
import jmri.TimebaseBehaviorTestBase;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/** Runs the shared Timebase behavior tests against SimpleTimebase, the reference behavior. */
public class SimpleTimebaseBehaviorTest extends TimebaseBehaviorTestBase {

    @Override
    protected Timebase createTimebase(InternalSystemConnectionMemo memo) {
        return new SimpleTimebase(memo);
    }

}
