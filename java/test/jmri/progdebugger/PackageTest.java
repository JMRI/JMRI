package jmri.progdebugger;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        jmri.progdebugger.DebugProgrammerTest.class,
        jmri.progdebugger.DebugProgrammerManagerTest.class,
        ProgDebuggerTest.class,
})

/**
 * Invoke complete set of tests for the Jmri.progdebugger package.
 * <p>
 * Due to existing package and class names, this is both the test suite for the
 * package, but also contains some tests for the ProgDebugger class.
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002
 */
public class PackageTest  {
}
