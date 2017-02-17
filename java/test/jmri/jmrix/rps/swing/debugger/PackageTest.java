package jmri.jmrix.rps.swing.debugger;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Tests for the jmri.jmrix.rps.swing.debugger package.
 *
 * @author Bob Jacobsen Copyright 2008
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DebuggerTest.class,
    DebuggerActionTest.class,
    DebuggerFrameTest.class,
    DebuggerTimePaneTest.class
})
public class PackageTest{
}
