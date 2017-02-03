package jmri.jmrix.dcc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;



/**
 * Tests for the jmri.jmrix.dcc package
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Paul Bender Copyright (C) 2017
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    DccTurnoutTest.class,
    DccTurnoutManagerTest.class,
    jmri.jmrix.dcc.configurexml.PackageTest.class
})
public class PackageTest {
}
